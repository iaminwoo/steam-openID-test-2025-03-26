import { useState } from "react";

const SteamLoginButton = () => {
  const [loading, setLoading] = useState(false);

  const handleLogin = async () => {
    setLoading(true);
    try {
      const response = await fetch("http://localhost:8080/api/auth/steam");
      const data = await response.json();
      window.location.href = data.redirectUrl; // Steam 로그인 페이지로 이동
    } catch (error) {
      console.error("Steam 로그인 요청 실패", error);
    }
    setLoading(false);
  };

  return (
    <button onClick={handleLogin} disabled={loading}>
      {loading ? "로그인 중..." : "Steam 로그인"}
    </button>
  );
};

export default SteamLoginButton;
