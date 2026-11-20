
import { importX509, jwtVerify } from "npm:jose@5";

const PROJECT_ID = "carloca2027";
const BUCKET = "carloca-files";

const SUPABASE_URL = Deno.env.get("SUPABASE_URL")!;
const SERVICE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers":
    "authorization, apikey, content-type, x-client-info",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

let cachedCertificates: Record<string, string> | null = null;
let certificatesExpiry = 0;

async function getFirebaseCertificates() {
  if (cachedCertificates && Date.now() < certificatesExpiry) {
    return cachedCertificates;
  }

  const response = await fetch(
    "https://www.googleapis.com/robot/v1/metadata/x509/securetoken@system.gserviceaccount.com"
  );

  if (!response.ok) {
    throw new Error("Firebase-Zertifikate konnten nicht geladen werden.");
  }

  cachedCertificates = await response.json();

  const cacheControl = response.headers.get("cache-control") || "";
  const maxAge = Number(
    cacheControl.match(/max-age=(\d+)/)?.[1] || "300"
  );

  certificatesExpiry = Date.now() + maxAge * 1000;

  return cachedCertificates!;
}

async function verifyFirebaseToken(token: string) {
  const { payload, protectedHeader } = await jwtVerify(
    token,
    async (header) => {
      const certificates = await getFirebaseCertificates();
      const certificate = certificates[header.kid || ""];

      if (!certificate) {
        throw new Error("Unbekannter Firebase-Signaturschlüssel.");
      }

      return await importX509(certificate, "RS256");
    },
    {
      issuer: `https://securetoken.google.com/${PROJECT_ID}`,
      audience: PROJECT_ID,
      algorithms: ["RS256"],
    }
  );

  if (!payload.sub || typeof payload.sub !== "string") {
    throw new Error("Firebase-Benutzerkennung fehlt.");
  }

  return payload.sub;
}

function jsonResponse(data: unknown, status = 200) {
  return new Response(JSON.stringify(data), {
    status,
    headers: {
      ...corsHeaders,
      "Content-Type": "application/json",
    },
  });
}

function safeSegment(value: string) {
  return value
    .replace(/[^a-zA-Z0-9_-]/g, "_")
    .slice(0, 100) || "allgemein";
}

function validateUserPath(path: string, userId: string) {
  const parts = path.split("/");

  return (
    parts.length === 5 &&
    parts[0] === userId &&
    ["vehicles", "repairs"].includes(parts[1]) &&
    parts.every(
      (part) => part.length > 0 && part !== "." && part !== ".."
    )
  );
}

Deno.serve(async (request) => {
  if (request.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  if (request.method !== "POST") {
    return jsonResponse({ error: "POST erforderlich." }, 405);
  }

  if (!SUPABASE_URL || !SERVICE_KEY) {
    return jsonResponse({
      error: "Supabase-Serverkonfiguration fehlt."
    }, 500);
  }

  const authorization = request.headers.get("Authorization");

  if (!authorization?.startsWith("Bearer ")) {
    return jsonResponse({ error: "Firebase-Anmeldung fehlt." }, 401);
  }

  let userId: string;

  try {
    userId = await verifyFirebaseToken(
      authorization.substring(7)
    );
  } catch (error) {
    console.error("Firebase-Token ungültig:", error);

    return jsonResponse({
      error: "Firebase-Anmeldung konnte nicht bestätigt werden."
    }, 401);
  }

  try {
    const contentType =
      request.headers.get("Content-Type") || "";

    // BILDER HERUNTERLADEN
    if (contentType.includes("application/json")) {
      const body = await request.json();

      if (body.action !== "download") {
        return jsonResponse({ error: "Ungültige Aktion." }, 400);
      }

      const storagePath = String(body.storagePath || "");

      if (!validateUserPath(storagePath, userId)) {
        return jsonResponse({
          error: "Kein Zugriff auf diese Datei."
        }, 403);
      }

      const encodedPath = storagePath
        .split("/")
        .map(encodeURIComponent)
        .join("/");

      const downloadResponse = await fetch(
        `${SUPABASE_URL}/storage/v1/object/${BUCKET}/${encodedPath}`,
        {
          method: "GET",
          headers: {
            apikey: SERVICE_KEY,
            Authorization: `Bearer ${SERVICE_KEY}`,
          },
        }
      );

      if (!downloadResponse.ok) {
        const error = await downloadResponse.text();

        return jsonResponse({
          error: "Bild konnte nicht heruntergeladen werden.",
          details: error,
        }, downloadResponse.status);
      }

      return new Response(downloadResponse.body, {
        status: 200,
        headers: {
          ...corsHeaders,
          "Content-Type":
            downloadResponse.headers.get("Content-Type") ||
            "application/octet-stream",
          "Cache-Control": "private, no-store",
        },
      });
    }

    // BILDER HOCHLADEN
    const form = await request.formData();

    const file = form.get("file");
    const category = String(form.get("category") || "");
    const vehicleId = String(form.get("vehicleId") || "");
    const recordId = String(form.get("recordId") || "");

    if (!(file instanceof File)) {
      return jsonResponse({ error: "Bilddatei fehlt." }, 400);
    }

    if (!["vehicles", "repairs"].includes(category)) {
      return jsonResponse({ error: "Ungültige Kategorie." }, 400);
    }

    if (!vehicleId || !recordId) {
      return jsonResponse({
        error: "Fahrzeug- oder Datensatz-ID fehlt."
      }, 400);
    }

    if (file.size <= 0 || file.size > 10 * 1024 * 1024) {
      return jsonResponse({
        error: "Das Bild darf maximal 10 MB groß sein."
      }, 400);
    }

    const allowedTypes = [
      "image/jpeg",
      "image/png",
      "image/webp",
    ];

    if (!allowedTypes.includes(file.type)) {
      return jsonResponse({
        error: "Dieses Bildformat wird nicht unterstützt."
      }, 400);
    }

    const extension =
      file.type === "image/png" ? "png" :
      file.type === "image/webp" ? "webp" : "jpg";

    const storagePath = [
      userId,
      category,
      safeSegment(vehicleId),
      safeSegment(recordId),
      `${crypto.randomUUID()}.${extension}`,
    ].join("/");

    const encodedPath = storagePath
      .split("/")
      .map(encodeURIComponent)
      .join("/");

    const uploadResponse = await fetch(
      `${SUPABASE_URL}/storage/v1/object/${BUCKET}/${encodedPath}`,
      {
        method: "POST",
        headers: {
          apikey: SERVICE_KEY,
          Authorization: `Bearer ${SERVICE_KEY}`,
          "Content-Type": file.type,
          "x-upsert": "true",
        },
        body: await file.arrayBuffer(),
      }
    );

    if (!uploadResponse.ok) {
      const error = await uploadResponse.text();

      return jsonResponse({
        error: "Supabase-Speicherung fehlgeschlagen.",
        details: error,
      }, uploadResponse.status);
    }

    return jsonResponse({
      success: true,
      storagePath,
    });

  } catch (error) {
    console.error("CARLOCA Upload-Fehler:", error);

    return jsonResponse({
      error: error instanceof Error
        ? error.message
        : "Unbekannter Serverfehler.",
    }, 500);
  }
});
