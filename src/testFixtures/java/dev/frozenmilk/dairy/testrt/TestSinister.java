package dev.frozenmilk.dairy.testrt;

import com.qualcomm.robotcore.util.RobotLog;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.frozenmilk.sinister.Preload;
import dev.frozenmilk.sinister.SinisterFilter;
import dev.frozenmilk.sinister.SinisterUtil;
import dev.frozenmilk.sinister.targeting.NarrowSearch;
import dev.frozenmilk.sinister.targeting.SearchTarget;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;

public class TestSinister {
	@NotNull
	private static final String TAG = "Sinister";
	@NotNull
	private final ArrayList<SinisterFilter> filters = new ArrayList<>();
	@NotNull
	private final ClassLoader loader = Objects.requireNonNull(Thread.currentThread().getContextClassLoader());
	@NotNull
	private final SearchTarget rootSearch = new NarrowSearch();
	private boolean run = false;
	public void run() {
		RobotLog.vv(TAG, "attempting boot on create");
		if (run) {
			RobotLog.vv(TAG, "already booted");
			System.out.print("\n\n---Sinister Finished---\n\n\n");
			return;
		}
		RobotLog.vv(TAG, "self booting...");
		rootSearch.exclude("worker.org.gradle");
		rootSearch.exclude("org.openftc");
		
		try (ScanResult scanResult =
				     new ClassGraph()
						     .enableAllInfo()
						     .acceptPackages()
						     .scan()
		) {
			ClassInfoList allClasses = scanResult.getAllClasses();
			for (ClassInfo classInfo : allClasses) {
				String name = classInfo.getName();
				if (rootSearch.determineInclusion(name)) {
					tryPreload(name);
				}
			}
			CompletableFuture<?>[] tasks = new CompletableFuture[filters.size()];
			ExecutorService executor = Executors.newWorkStealingPool();
			for (int i = 0; i < filters.size(); i++) {
				tasks[i] = spawnFilter(filters.get(i), allClasses.iterator(), executor);
			}
			try {
				for (CompletableFuture<?> task : tasks) {
					CompletableFuture<?> res = (CompletableFuture<?>) task.get();
					while (res != null) {
						res = (CompletableFuture<?>) res.get();
					}
				}
			}
			catch (Throwable ignored) {
			}
		}
		RobotLog.vv(TAG, "...booted");
		run = true;
		RobotLog.vv(TAG, "finished boot process");
		System.out.print("\n\n---Sinister Finished---\n\n\n");
	}
	private void tryPreload(String className) {
		Class<?> c;
		try {
			c = Class.forName(className);
		} catch (ClassNotFoundException | ExceptionInInitializerError e) {
			return;
		}
		if (SinisterUtil.inheritsAnnotation(c, Preload.class)) {
			try {
				loader.loadClass(c.getName());
			} catch (ClassNotFoundException e) {
				return;
			}
			String simpleName = c.getSimpleName();
			RobotLog.vv(TAG, "preloading: " + simpleName);
			
			if (SinisterFilter.class.isAssignableFrom(c)) {
				List<SinisterFilter> foundFilters = SinisterUtil.staticInstancesOf(c, SinisterFilter.class);
				if (!foundFilters.isEmpty()) {
					foundFilters.forEach(filter -> {
						RobotLog.vv(TAG, "found filter " + simpleName);
						filter.init();
					});
					filters.addAll(foundFilters);
				}
			}
		}
	}
	private CompletableFuture<CompletableFuture<?>> spawnFilter(SinisterFilter filter, @NotNull Iterator<ClassInfo> allClasses, ExecutorService executor) {
		ClassInfo info = allClasses.next();
		return CompletableFuture.runAsync(() -> {
					if (filter.getTargets().determineInclusion(info.getName())) {
						synchronized(filter.getLock()) {
							filter.filter(info.loadClass(true));
						}
					}
				}, executor)
				.handle((res, err) -> {
					// ignore err
//					if (err != null) RobotLog.ee(TAG, "Error occurred while running SinisterFilter: ${filter::class.simpleName} | ${filter}\nFiltering Class:${cls}\nError: $err\nStackTrace: ${err.stackTraceToString()}");
					if (allClasses.hasNext()) return spawnFilter(filter, allClasses, executor);
					return null;
				});
	}
}
