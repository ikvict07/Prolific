package org.nevertouchgrass.prolific.util;

import lombok.Getter;
import lombok.Setter;
import oshi.SystemInfo;
import oshi.software.os.OSProcess;

@Getter
public class ProcessWrapper {
    private final OSProcess osProcess;
    @Setter
    private Process process;
    @Setter
    private String name;

    public ProcessWrapper(OSProcess osProcess) {
        this.osProcess = osProcess;
    }

    public static ProcessWrapper of(Process process) {
        var osProcess = new SystemInfo().getOperatingSystem().getProcess((int) process.pid());
        var inst = new ProcessWrapper(osProcess);
        inst.setProcess(process);
        return inst;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ProcessWrapper that = (ProcessWrapper) obj;
        return this.osProcess.getProcessID() == that.osProcess.getProcessID();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(osProcess.getProcessID());
    }
}
