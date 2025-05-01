package org.nevertouchgrass.prolific.util;

import java.util.HashSet;
import java.util.Set;

public class ProcessUtl {
    private ProcessUtl() {}

    public static void receiveDescendants(ProcessHandle process, Set<ProcessHandle> descendantsToReceive) {
        var d = new HashSet<ProcessHandle>();
        var children = process.children().toList();
        var descendants = process.descendants().toList();
        d.add(process);
        d.addAll(children);
        d.addAll(descendants);
        descendantsToReceive.addAll(d);
        children.forEach(c -> receiveDescendants(c, descendantsToReceive));
        children.forEach(c -> receiveDescendants(c, descendantsToReceive));
    }

}
