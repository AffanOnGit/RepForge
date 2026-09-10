const VIDEO_ID_PATTERN =
  /^.*(?:(?:youtu\.be\/|v\/|vi\/|u\/\w\/|embed\/|shorts\/)|(?:(?:watch)?\?v(?:i)?=|&v(?:i)?=))([^#&?]*).*/i;

export function extractYouTubeVideoId(url: string): string | null {
  const trimmed = url.trim();
  if (trimmed.length === 11 && !trimmed.includes('/')) {
    return trimmed;
  }

  const match = trimmed.match(VIDEO_ID_PATTERN);
  const id = match?.[1];
  return id && id.length === 11 ? id : null;
}

export function isLikelyYouTubeUrl(input: string): boolean {
  const t = input.trim().toLowerCase();
  return (
    t.includes('youtube.com') ||
    t.includes('youtu.be') ||
    (t.length === 11 && /^[a-zA-Z0-9_-]+$/.test(t))
  );
}

export function youtubeThumbnailUrl(videoId: string): string {
  return `https://img.youtube.com/vi/${videoId}/hqdefault.jpg`;
}
