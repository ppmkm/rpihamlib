package pl.sp9muf.udpserver;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/*
rpihamlib,  set of tools (glue) to create rigctl cotrollable  transceiver 
from rictl controllable receiver and rpitx as transmittter
Copyright (C) 2020 Piotr Mis

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/


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
