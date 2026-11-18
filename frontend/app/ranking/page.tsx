import { redirect } from "next/navigation";

const PLATFORMS = [
  { slug: "youtube" },
  { slug: "instagram" },
  { slug: "threads" },
  { slug: "x" },
  { slug: "tiktok" },
];

const CATEGORIES = [
  { slug: "sports" },
  { slug: "food" },
  { slug: "pet" },
  { slug: "finance" },
  { slug: "family" },
  { slug: "ai" },
  { slug: "travel" },
];

// /ranking은 첫 번째 플랫폼의 첫 번째 카테고리로 보낸다.
export default function RankingPage() {
  const [platform] = PLATFORMS;
  const [category] = CATEGORIES;

  redirect(`/ranking/${platform.slug}/${category.slug}`);
}
