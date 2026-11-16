import { redirect } from "next/navigation";

// /feed는 롱폼 피드로 보낸다.
export default function FeedPage() {
  redirect("/feed/long");
}
