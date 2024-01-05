package cc.nnproject.starter;

import ru.lgame.launcher.Launcher;

public class LauncherStart {
	
	public static void main(String[] args) {
		Launcher.starter = true;
		new Launcher().startLauncher();
	}

}
