"use client";

import SteamLoginButton from "@/components/SteamLoginButton";

export default function Home() {
  return (
    <div>
      <h1 className="text-lg">Steam OpenID 로그인</h1>
      <div className="py-4">
        <SteamLoginButton />
      </div>
    </div>
  );
}
