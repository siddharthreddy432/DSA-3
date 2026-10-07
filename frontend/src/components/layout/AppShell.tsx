import React, { useEffect, useRef } from 'react';
import Lenis from 'lenis';
import type { ViewMode } from '../../types/engine';
import { Navigation } from './Navigation';
import { TopBar } from './TopBar';
import { CustomCursor } from './CustomCursor';

interface AppShellProps {
  currentView: ViewMode;
  onSelectView: (view: ViewMode) => void;
  children: React.ReactNode;
}

export const AppShell: React.FC<AppShellProps> = ({
  currentView,
  onSelectView,
  children
}) => {
  const mainRef = useRef<HTMLElement>(null);

  useEffect(() => {
    // Respect prefers-reduced-motion
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
      return;
    }

    // Initialize Lenis smooth scroll
    const lenis = new Lenis({
      duration: 1.0,
      easing: (t) => Math.min(1, 1.001 - Math.pow(2, -10 * t)),
      smoothWheel: true
    });

    let rafId: number;
    function raf(time: number) {
      lenis.raf(time);
      rafId = requestAnimationFrame(raf);
    }
    rafId = requestAnimationFrame(raf);

    return () => {
      cancelAnimationFrame(rafId);
      lenis.destroy();
    };
  }, []);

  return (
    <div className="app-container">
      <CustomCursor />
      <Navigation currentView={currentView} onSelectView={onSelectView} />
      <main className="app-main" ref={mainRef}>
        <TopBar currentView={currentView} onSelectView={onSelectView} />
        <div className="content-wrapper">
          {children}
        </div>
      </main>
    </div>
  );
};
