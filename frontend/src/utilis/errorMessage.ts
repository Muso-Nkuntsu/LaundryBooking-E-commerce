// Turns a thrown error into a sentence a student can act on.
export function friendlyError(error: unknown, fallback: string): string {
  if (typeof error === "object" && error !== null) {
    const maybe = error as {
      code?: string;
      message?: string;
      response?: { status?: number; data?: unknown };
    };

    // The server answered with an error (axios).
    if (maybe.response) {
      const data = maybe.response.data;
      if (typeof data === "string" && data.trim()) return data;
      if (typeof data === "object" && data !== null && "message" in data) {
        const message = (data as { message?: unknown }).message;
        if (typeof message === "string" && message.trim()) return message;
      }
      return fallback;
    }

    // The request never got an answer: server down, wrong port, or blocked by CORS.
    const message = maybe.message ?? "";
    if (maybe.code === "ERR_NETWORK" || error instanceof TypeError || /network error|failed to fetch|load failed/i.test(message)) {
      return "Can't reach the server. Check that the backend is running, then try again.";
    }

    if (message) return message;
  }

  return fallback;
}
