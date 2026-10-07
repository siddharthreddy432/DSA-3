package engine;

import engine.datastructures.DynamicArray;
import engine.ingestion.IOCLoader;
import engine.ingestion.LogParser;
import engine.ingestion.SignatureMatcher;
import engine.ingestion.SignatureMatcher.Algorithm;
import engine.models.IOC;
import engine.models.LogEntry;
import engine.models.SignatureMatch;

import java.io.File;

/**
 * Executable demonstration entrypoint for the Threat-Intelligence Kill-Chain Engine.
 *
 * <p>Phase 3 demonstration:
 * <ol>
 *   <li>Load raw security logs via {@link LogParser} into {@link DynamicArray}</li>
 *   <li>Load threat indicators via {@link IOCLoader} into {@link DynamicArray}</li>
 *   <li>Execute first-principles string matching via {@link SignatureMatcher}</li>
 *   <li>Print structured signature matches</li>
 * </ol>
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("Threat Intelligence Kill-Chain Correlation Engine");
        System.out.println("Phase 3: String Matching & IoC Detection");
        System.out.println("==================================================");
        System.out.println();

        String logsPath = "data" + File.separator + "input" + File.separator + "logs.txt";
        String iocsPath = "data" + File.separator + "input" + File.separator + "iocs.txt";

        try {
            System.out.println("[INFO] Ingesting logs from: " + logsPath);
            DynamicArray<LogEntry> logs = LogParser.parseFile(logsPath);
            System.out.println("[INFO] Successfully ingested " + logs.size() + " log entries.");

            System.out.println("[INFO] Loading indicators from: " + iocsPath);
            DynamicArray<IOC> iocs = IOCLoader.loadFile(iocsPath);
            System.out.println("[INFO] Successfully loaded " + iocs.size() + " IoC definitions.");

            System.out.println();
            System.out.println("[INFO] Executing String Matching Engine (Algorithm: KMP)...");
            DynamicArray<SignatureMatch> matches = SignatureMatcher.matchAll(logs, iocs, Algorithm.KMP);
            System.out.println("[INFO] Total Signature Matches Detected: " + matches.size());
            System.out.println();

            for (int i = 0; i < matches.size(); i++) {
                SignatureMatch match = matches.get(i);
                System.out.println("----------------------------------------");
                System.out.print(match.toReportString());
            }
            System.out.println("----------------------------------------");
            System.out.println("[INFO] Phase 3 Pipeline execution complete.");

        } catch (Exception e) {
            System.err.println("[ERROR] Pipeline execution failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
