import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import Hero from "../components/landing/Hero";
import Features from "../components/landing/Features";
import ClaimLifecycle from "../components/landing/ClaimLifecycle";
import About from "../components/landing/About";
import CTA from "../components/landing/CTA";

export default function LandingPage() {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (isAuthenticated) {
      navigate("/dashboard", { replace: true });
    }
  }, [isAuthenticated, navigate]);

  return (
    <>
      <Hero />
      <Features />
      <ClaimLifecycle />
      <About />
      <CTA />
    </>
  );
}
