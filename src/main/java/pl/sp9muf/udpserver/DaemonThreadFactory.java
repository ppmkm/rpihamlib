package pl.sp9muf.udpserver;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class DaemonThreadFactory implements ThreadFactory {

	private final String namePrefix;
	private final ThreadGroup group;
    final AtomicInteger threadNumber = new AtomicInteger(1);

	public DaemonThreadFactory(String prefix) {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() :
                              Thread.currentThread().getThreadGroup();
        namePrefix = prefix;
	}

	@Override
	public Thread newThread(Runnable r) {
        Thread t = new Thread(group,
                r,
                namePrefix +
                threadNumber.getAndIncrement(),
                0);
        t.setDaemon(true);
        if (t.getPriority() != Thread.NORM_PRIORITY)
        	t.setPriority(Thread.NORM_PRIORITY);
        return t;
	}

}
