package org.nevertouchgrass.prolific.util;

import oshi.software.os.OSProcess;

public class OSProcessWrapper {
    private final OSProcess process;

    public OSProcessWrapper(OSProcess process) {
        this.process = process;
    }

    public OSProcess getProcess() {
        return process;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        OSProcessWrapper that = (OSProcessWrapper) obj;
        return this.process.getProcessID() == that.process.getProcessID();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(process.getProcessID());
    }
}
