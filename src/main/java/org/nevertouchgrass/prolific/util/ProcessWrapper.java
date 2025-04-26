package org.nevertouchgrass.prolific.util;

import lombok.Getter;
import lombok.Setter;
import oshi.SystemInfo;
import oshi.software.os.OSProcess;

@Getter
public class ProcessWrapper {
    @Setter
    private Process process;
    @Setter
    private String name;

    public int getPid() {

        return (int) process.pid();
    }


    public static ProcessWrapper of(Process process) {
        var inst = new ProcessWrapper();
        inst.setProcess(process);
        return inst;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ProcessWrapper that = (ProcessWrapper) obj;
        return this.getPid() == that.getPid();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(getPid());
    }
}
