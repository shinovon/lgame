package cc.nnproject.starter;

import ru.lgame.launcher.Launcher;
import ru.lgame.launcher.main.Main;

public class LauncherStart {
	
	public static void main(String[] args) {
		Launcher.starter = true;
		Main.main_(args);
	}

}
