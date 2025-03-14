package org.nevertouchgrass.prolific.metrix;

import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

// oshi version 6.6.5

public class Main {

    static OshiSetup oshiSetup;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        List<String> paths = Arrays.asList(
                "D:\\_Code\\FIIT\\JAVA\\2024_2025\\LS\\VAVA\\TestProjects",
                "D:\\_Code\\FIIT\\JAVA\\2024_2025\\LS\\VAVA\\settings"
        );
        oshiSetup = new OshiSetup();
        List<Pair<Integer, OSProcess>> processes = new ArrayList<>();
        OSProcess tmpProcess;
        while (true){
            oshiSetup.getProcesses(paths, processes);

            for (Pair<Integer, OSProcess> pair : processes) {
                tmpProcess = pair.getB();
                if(!tmpProcess.updateAttributes()) break; // remove from processes
                System.out.printf("For project with ID (%d):\n", pair.getA());
                System.out.println(OshiSetup.getProcessMetrix(tmpProcess));

            }
            System.out.println("_________________________________________________________\n");

            {
            try {
                // Check for user input to exit
                if (System.in.available() > 0) {
                    scanner.nextLine();
                    break;
                }
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                System.out.println("Monitoring stopped.");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
            }
        }
    }
}

class OshiSetup {
    protected final SystemInfo systemInfo;
    protected final OperatingSystem os;
    private List<OSProcess> processes;

    public OshiSetup() {
        systemInfo = new SystemInfo();
        os = systemInfo.getOperatingSystem();
    }

    public void getProcesses(List<String> paths, List<Pair<Integer, OSProcess>> result) {
        result.clear();

        processes = os.getProcesses();

        result.addAll(processes.parallelStream()
                .filter(process -> process.getCommandLine().contains("java"))
                .flatMap(process -> paths.stream()
                        .filter(path -> process.getCurrentWorkingDirectory().contains(path))
                        .map(path -> new Pair<>(paths.indexOf(path), process)))
                .toList());
    }

    // https://javadoc.io/doc/com.github.oshi/oshi-core/6.6.5/oshi/software/os/OSProcess.html
    public static ProcessMetrix getProcessMetrix(OSProcess process) {
        return new ProcessMetrix(process.getName(),process.getUser(),process.getThreadCount(),process.getBytesRead(),process.getBytesWritten(),process.getProcessCpuLoadCumulative()*100,process.getVirtualSize());
    }
}