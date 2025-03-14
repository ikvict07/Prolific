package org.nevertouchgrass.prolific.metrix;

record ProcessMetrix(String name, String user, int threadCount, long bytesRead, long bytesWritten,
                     double processCpuLoadCumulative, long virtualSize) {
    @Override
    public String toString() {
        return String.format("%s %s %d |%d %d| CPU:%f MEM: %f MB", name, user, threadCount, bytesRead, bytesWritten, processCpuLoadCumulative,virtualSize/(1024.0 * 1024.0));
    }
}