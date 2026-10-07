import React, { useState, useEffect, useRef } from 'react';
import gsap from 'gsap';
import type { ViewMode } from './types/engine';
import { AppShell } from './components/layout/AppShell';
import { OverviewView } from './components/views/OverviewView';
import { IngestionView } from './components/views/IngestionView';
import { SignatureMatchingView } from './components/views/SignatureMatchingView';
import { KillChainView } from './components/views/KillChainView';
import { AttackGraphView } from './components/views/AttackGraphView';
import { ChokePointView } from './components/views/ChokePointView';
import { MonitoringView } from './components/views/MonitoringView';
import { AlertRoutingView } from './components/views/AlertRoutingView';

export const App: React.FC = () => {
  const [currentView, setCurrentView] = useState<ViewMode>('overview');
  const viewContainerRef = useRef<HTMLDivElement>(null);

  // Subtle entrance reveal on view change via GSAP
  useEffect(() => {
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
      return;
    }

    if (viewContainerRef.current) {
      gsap.fromTo(
        viewContainerRef.current,
        { opacity: 0.85, y: 4 },
        { opacity: 1, y: 0, duration: 0.25, ease: 'power2.out' }
      );
    }
  }, [currentView]);

  const renderActiveView = () => {
    switch (currentView) {
      case 'overview':
        return <OverviewView onNavigate={setCurrentView} />;
      case 'ingestion':
        return <IngestionView />;
      case 'signatures':
        return <SignatureMatchingView />;
      case 'killchain':
        return <KillChainView />;
      case 'graph':
        return <AttackGraphView />;
      case 'chokepoints':
        return <ChokePointView />;
      case 'monitoring':
        return <MonitoringView />;
      case 'alerts':
        return <AlertRoutingView />;
      default:
        return <OverviewView onNavigate={setCurrentView} />;
    }
  };

  return (
    <AppShell currentView={currentView} onSelectView={setCurrentView}>
      <div ref={viewContainerRef} key={currentView}>
        {renderActiveView()}
      </div>
    </AppShell>
  );
};

export default App;
