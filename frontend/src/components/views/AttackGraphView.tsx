import React, { useState } from 'react';
import { ATTACK_NODES, ATTACK_EDGES } from '../../data/sampleData';
import { SectionHeader } from '../common/SectionHeader';
import { StatusBadge } from '../common/StatusBadge';
import { ZoomIn, ZoomOut, RotateCcw } from 'lucide-react';

export const AttackGraphView: React.FC = () => {
  const [selectedNodeId, setSelectedNodeId] = useState<string>('HOST-014');
  const [zoomLevel, setZoomLevel] = useState<number>(1);
  const [panOffset, setPanOffset] = useState<{ x: number; y: number }>({ x: 0, y: 0 });
  const [isDragging, setIsDragging] = useState<boolean>(false);
  const [dragStart, setDragStart] = useState<{ x: number; y: number }>({ x: 0, y: 0 });

  const selectedNode = ATTACK_NODES.find((n) => n.id === selectedNodeId) || ATTACK_NODES[0];

  // Edges directly connected to the selected node
  const connectedEdges = ATTACK_EDGES.filter(
    (e) => e.source === selectedNode.id || e.target === selectedNode.id
  );

  const handleZoomIn = () => setZoomLevel((prev) => Math.min(prev + 0.15, 2.0));
  const handleZoomOut = () => setZoomLevel((prev) => Math.max(prev - 0.15, 0.6));
  const handleReset = () => {
    setZoomLevel(1);
    setPanOffset({ x: 0, y: 0 });
    setSelectedNodeId('HOST-014');
  };

  const handleMouseDown = (e: React.MouseEvent<SVGSVGElement>) => {
    // Only drag if clicking background SVG
    if ((e.target as HTMLElement).tagName === 'svg' || (e.target as HTMLElement).id === 'graph-bg') {
      setIsDragging(true);
      setDragStart({ x: e.clientX - panOffset.x, y: e.clientY - panOffset.y });
    }
  };

  const handleMouseMove = (e: React.MouseEvent<SVGSVGElement>) => {
    if (isDragging) {
      setPanOffset({
        x: e.clientX - dragStart.x,
        y: e.clientY - dragStart.y
      });
    }
  };

  const handleMouseUp = () => setIsDragging(false);

  return (
    <div>
      <SectionHeader
        tag="STAGE 04 — TOPOLOGY CORRELATION"
        title="Lateral Movement Attack Graph"
        description="Directed graph visualization representing host compromise transitions, pivotal jumpbox hops, and reachable paths toward target database assets."
        actionRow={
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <StatusBadge type="simulated" label="GRAPH SIMULATION" />
          </div>
        }
      />

      {/* Main Graph Canvas & Inspector Grid */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: '1fr 310px',
        gap: '16px',
        alignItems: 'start'
      }}>
        {/* SVG Graph Viewport */}
        <div className="graph-viewport-card" style={{ position: 'relative' }}>
          {/* Zoom and Reset Controls Overlay */}
          <div className="graph-controls-overlay">
            <button
              className="graph-btn"
              onClick={handleZoomIn}
              title="Zoom In"
              aria-label="Zoom in"
            >
              <ZoomIn size={15} />
            </button>
            <button
              className="graph-btn"
              onClick={handleZoomOut}
              title="Zoom Out"
              aria-label="Zoom out"
            >
              <ZoomOut size={15} />
            </button>
            <button
              className="graph-btn"
              onClick={handleReset}
              title="Reset View"
              aria-label="Reset graph view"
            >
              <RotateCcw size={15} />
            </button>
          </div>

          {/* Interactive SVG Canvas */}
          <svg
            width="100%"
            height="100%"
            viewBox="0 0 1020 480"
            onMouseDown={handleMouseDown}
            onMouseMove={handleMouseMove}
            onMouseUp={handleMouseUp}
            onMouseLeave={handleMouseUp}
            style={{
              cursor: isDragging ? 'grabbing' : 'grab',
              userSelect: 'none'
            }}
          >
            <rect id="graph-bg" width="100%" height="100%" fill="transparent" />

            <g transform={`translate(${panOffset.x}, ${panOffset.y}) scale(${zoomLevel})`}>
              {/* Render Edges */}
              {ATTACK_EDGES.map((edge) => {
                const sourceNode = ATTACK_NODES.find((n) => n.id === edge.source);
                const targetNode = ATTACK_NODES.find((n) => n.id === edge.target);
                if (!sourceNode || !targetNode) return null;

                const isConnected =
                  edge.source === selectedNodeId || edge.target === selectedNodeId;

                return (
                  <g key={edge.id}>
                    <line
                      x1={sourceNode.x}
                      y1={sourceNode.y}
                      x2={targetNode.x}
                      y2={targetNode.y}
                      stroke={
                        isConnected
                          ? 'var(--accent)'
                          : edge.isBottleneck
                          ? 'rgba(255, 255, 255, 0.28)'
                          : 'rgba(255, 255, 255, 0.12)'
                      }
                      strokeWidth={isConnected ? 2 : edge.isBottleneck ? 1.5 : 1}
                      strokeDasharray={edge.protocol === 'RDP' ? '4 2' : undefined}
                    />
                    {/* Edge Protocol Tag */}
                    <text
                      x={(sourceNode.x + targetNode.x) / 2}
                      y={(sourceNode.y + targetNode.y) / 2 - 6}
                      fill={isConnected ? 'var(--accent)' : 'var(--text-muted)'}
                      fontSize="9px"
                      fontFamily="var(--font-mono)"
                      textAnchor="middle"
                    >
                      {edge.protocol}
                    </text>
                  </g>
                );
              })}

              {/* Render Nodes */}
              {ATTACK_NODES.map((node) => {
                const isSelected = node.id === selectedNodeId;
                const isChoke = node.id === 'HOST-014';
                const isTarget = node.type === 'target';
                const isEntry = node.type === 'entry';

                return (
                  <g
                    key={node.id}
                    transform={`translate(${node.x}, ${node.y})`}
                    onClick={() => setSelectedNodeId(node.id)}
                    style={{ cursor: 'pointer' }}
                    data-clickable="true"
                  >
                    {/* Outer Selection / Choke Ring */}
                    {isSelected && (
                      <circle
                        r="22"
                        fill="none"
                        stroke="var(--accent)"
                        strokeWidth="1.5"
                        strokeDasharray="3 2"
                      />
                    )}

                    {isChoke && !isSelected && (
                      <circle
                        r="20"
                        fill="none"
                        stroke="var(--severity-high)"
                        strokeWidth="1"
                        strokeOpacity="0.6"
                      />
                    )}

                    {/* Node Core Body */}
                    <circle
                      r={isTarget || isEntry || isChoke ? 14 : 11}
                      fill={
                        isSelected
                          ? 'var(--surface-elevated)'
                          : isChoke
                          ? 'var(--surface-elevated)'
                          : 'var(--surface)'
                      }
                      stroke={
                        isSelected
                          ? 'var(--accent)'
                          : isTarget
                          ? 'var(--text-primary)'
                          : isChoke
                          ? 'var(--severity-high)'
                          : 'var(--border-strong)'
                      }
                      strokeWidth={isSelected ? 2 : 1}
                    />

                    {/* Internal Indicator Dot */}
                    <circle
                      r="3.5"
                      fill={
                        isSelected
                          ? 'var(--accent)'
                          : isChoke
                          ? 'var(--severity-high)'
                          : isTarget
                          ? 'var(--text-primary)'
                          : 'var(--text-muted)'
                      }
                    />

                    {/* Node ID Label */}
                    <text
                      y={26}
                      fill={isSelected ? 'var(--text-primary)' : 'var(--text-secondary)'}
                      fontSize="11px"
                      fontFamily="var(--font-mono)"
                      fontWeight={isSelected ? '600' : '400'}
                      textAnchor="middle"
                    >
                      {node.label}
                    </text>

                    {/* Node IP Sublabel */}
                    <text
                      y={38}
                      fill="var(--text-muted)"
                      fontSize="9px"
                      fontFamily="var(--font-mono)"
                      textAnchor="middle"
                    >
                      {node.ip}
                    </text>
                  </g>
                );
              })}
            </g>
          </svg>

          {/* Graph Legend */}
          <div style={{
            position: 'absolute',
            bottom: '12px',
            left: '14px',
            display: 'flex',
            alignItems: 'center',
            gap: '14px',
            fontSize: '11px',
            fontFamily: 'var(--font-mono)',
            color: 'var(--text-muted)',
            backgroundColor: 'rgba(14, 16, 18, 0.85)',
            padding: '6px 12px',
            borderRadius: 'var(--radius-xs)',
            border: '1px solid var(--border)'
          }}>
            <span>● Normal Host</span>
            <span style={{ color: 'var(--severity-high)' }}>● Choke Bridge (HOST-014)</span>
            <span style={{ color: 'var(--text-primary)' }}>● Target Sink (HOST-031)</span>
          </div>
        </div>

        {/* Node Inspector Panel */}
        <aside style={{
          padding: '20px',
          backgroundColor: 'var(--surface)',
          border: '1px solid var(--border)',
          borderRadius: 'var(--radius-sm)'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '14px' }}>
            <span className="tech-label" style={{ color: 'var(--text-muted)' }}>NODE INSPECTOR</span>
            <StatusBadge
              type={selectedNode.id === 'HOST-014' ? 'high' : 'verified'}
              label={selectedNode.status}
            />
          </div>

          <h2 style={{ fontSize: '18px', fontWeight: 600, color: 'var(--text-primary)', marginBottom: '4px' }}>
            {selectedNode.label}
          </h2>
          <div className="mono-text" style={{ fontSize: '12px', color: 'var(--accent)', marginBottom: '16px' }}>
            {selectedNode.ip}
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', fontSize: '12px', borderTop: '1px solid var(--border)', paddingTop: '12px' }}>
            <div>
              <span className="mono-text" style={{ color: 'var(--text-muted)', display: 'block', marginBottom: '2px' }}>ROLE / STAGE</span>
              <span style={{ color: 'var(--text-primary)' }}>{selectedNode.stage}</span>
            </div>

            <div>
              <span className="mono-text" style={{ color: 'var(--text-muted)', display: 'block', marginBottom: '2px' }}>ADJACENT CONNECTIONS</span>
              <span className="mono-text" style={{ color: 'var(--text-primary)' }}>{selectedNode.connectionsCount} edges</span>
            </div>

            <div>
              <span className="mono-text" style={{ color: 'var(--text-muted)', display: 'block', marginBottom: '2px' }}>REACHABLE ATTACK PATHS</span>
              <span className="mono-text" style={{ color: selectedNode.reachablePathsCount >= 7 ? 'var(--severity-critical)' : 'var(--text-primary)' }}>
                {selectedNode.reachablePathsCount} paths traversing node
              </span>
            </div>
          </div>

          {/* Connected Edges Breakdown */}
          <div style={{ marginTop: '16px', borderTop: '1px solid var(--border)', paddingTop: '12px' }}>
            <span className="tech-label" style={{ color: 'var(--text-muted)', display: 'block', marginBottom: '8px' }}>
              INCIDENT TRANSITIONS ({connectedEdges.length})
            </span>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              {connectedEdges.map((edge) => (
                <div
                  key={edge.id}
                  style={{
                    padding: '6px 8px',
                    backgroundColor: 'var(--surface-elevated)',
                    border: '1px solid var(--border)',
                    borderRadius: 'var(--radius-xs)',
                    fontSize: '11px',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between'
                  }}
                >
                  <span className="mono-text" style={{ color: 'var(--text-secondary)' }}>
                    {edge.source} → {edge.target}
                  </span>
                  <span className="tech-label" style={{ color: 'var(--accent)' }}>
                    {edge.protocol}
                  </span>
                </div>
              ))}
            </div>
          </div>
        </aside>
      </div>
    </div>
  );
};
