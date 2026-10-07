/**
 * Data structures and view types for the Threat-Intelligence Kill-Chain Correlation Engine.
 */

export type ViewMode =
  | 'overview'
  | 'ingestion'
  | 'signatures'
  | 'killchain'
  | 'graph'
  | 'chokepoints'
  | 'monitoring'
  | 'alerts';

export type LogCategory = 'ALL' | 'AUTH' | 'EXPLOIT' | 'PRIVILEGE' | 'LATERAL' | 'EXFIL';

export type Severity = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';

export interface LogEntry {
  id: string;
  time: string;
  sourceIp: string;
  targetHost: string;
  category: Exclude<LogCategory, 'ALL'>;
  payload: string;
  rawDetails: string;
  severity: Severity;
}

export type MatchingAlgorithm = 'KMP' | 'Z_ALGORITHM' | 'RABIN_KARP';

export interface AlgorithmSpec {
  id: MatchingAlgorithm;
  name: string;
  complexityTime: string;
  complexitySpace: string;
  theoreticalStatus: string;
  description: string;
  preprocessingSummary: string;
}

export interface SignatureMatch {
  id: string;
  pattern: string;
  logId: string;
  timestamp: string;
  host: string;
  offset: number;
  length: number;
  matchedSnippet: string;
  algorithmUsed: MatchingAlgorithm;
}

export interface KillChainStage {
  id: string;
  stageNumber: number;
  name: string;
  summary: string;
  timestamp: string;
  primaryHost: string;
  evidence: string;
  status: 'CORRELATED' | 'CONFIRMED' | 'STAGED';
  indicators: string[];
}

export interface AttackNode {
  id: string;
  label: string;
  ip: string;
  type: 'entry' | 'compromised' | 'pivot' | 'target' | 'exfil';
  stage: string;
  x: number;
  y: number;
  connectionsCount: number;
  reachablePathsCount: number;
  status: 'ACTIVE_PIVOT' | 'ISOLATED' | 'TARGET_SINK' | 'ENTRY_POINT';
}

export interface AttackEdge {
  id: string;
  source: string;
  target: string;
  protocol: 'SSH' | 'SMB' | 'RDP' | 'HTTPS';
  bandwidthWeight: number;
  isBottleneck: boolean;
  activityDescription: string;
}

export interface ChokePoint {
  hostId: string;
  ip: string;
  role: string;
  reachablePaths: number;
  totalPaths: number;
  articulationScore: string;
  minCutCapacity: number;
  cutStatus: 'PRIMARY_BOTTLENECK' | 'SECONDARY_PIVOT';
}

export interface MonitoringRecommendation {
  rank: number;
  sensorHost: string;
  pathsCovered: number;
  totalPaths: number;
  coveragePercentage: number;
  sensorType: 'NETWORK_TAP' | 'HOST_EDR' | 'AUTH_WATCHER';
  rationale: string;
}

export interface AlertItem {
  id: string;
  severity: Severity;
  timestamp: string;
  hostId: string;
  stage: string;
  title: string;
  description: string;
  mitigation: string;
  status: 'UNRESOLVED' | 'INVESTIGATING' | 'MITIGATED';
}
