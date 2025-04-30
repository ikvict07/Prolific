# Settings

![](./assets/settings/general/settings.png)

The Settings screen is where you can configure a variety of options for the app. The settings are divided into several
categories:

## General

- **Root path to scan**: This is one of the most important settings. It defines the root path where the app will scan
  for files. The app will look for files in this directory and its subdirectories. You can set this to any directory on
  your system. You can use
  a <svg width="16" height="16" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg" style="vertical-align: middle;"><path d="M3.25 3.25H7.6377C7.6817 3.25 7.72498 3.2615 7.7627 3.2832L7.79785 3.30762L10.5195 5.57617L10.7285 5.75H17C17.6904 5.75 18.25 6.30964 18.25 7V15.167C18.2498 16.0672 17.5529 16.75 16.75 16.75H3.25C2.44714 16.75 1.75017 16.0672 1.75 15.167V4.83301C1.75017 3.93278 2.44715 3.25 3.25 3.25Z" stroke="#CED0D6" stroke-width="1.5"/></svg>
  button to select the directory.

- **Excluded dirs**: This setting allows you to specify directories that should be excluded from the scan. You can add
  multiple directories, and they will be ignored during the scan process. You should use a semicolon (;) to separate
  multiple directories. For example: `dir1;dir2;dir3`. The app will not scan these directories or any of their
  subdirectories. This field also supports a glob pattern, so you can use wildcards to exclude multiple directories that
  match a certain pattern. For example: `dir*;dir2*` will exclude all directories that start with `dir` or `dir2`.

- **Max scan depth**: This setting allows you to specify the maximum depth of subdirectories to scan. For example, if
  you set this to 2, the app will scan the root directory and its immediate subdirectories, but not any subdirectories
  of those subdirectories. This can be useful if you have a large directory structure and want to limit the scan to a
  certain level.

- **Rescan every hours**: App automatically rescan the root path every X hours. This is useful if you want to keep the
  app up to date with the latest changes in your files. You can set this to any number of hours. Keep in mind that
  rescanning will be performed only if the app restarts. If you want to rescan the root path immediately, you can use
  the "Run scanning" button in the app.
- **Language**: We currently support English, Slovak and Russian. You can select the language from the dropdown menu. No
  restart is required

## Environment

![environment.png](assets/settings/environment/environment.png)

This section allows you to configure the environment settings for the app.
But for common use, you don't need to change anything here.

- **Python path**: *Default value is python from PATH.* If you want to use some python binary (different from one that is in PATH) you can configure this
  here. This is useful if you have multiple versions of Python installed on your system or if you want to use a specific
  version of Python for the app. You can set this to any valid path to a Python binary. You can use
  a <svg width="16" height="16" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg" style="vertical-align: middle;"><path d="M3.25 3.25H7.6377C7.6817 3.25 7.72498 3.2615 7.7627 3.2832L7.79785 3.30762L10.5195 5.57617L10.7285 5.75H17C17.6904 5.75 18.25 6.30964 18.25 7V15.167C18.2498 16.0672 17.5529 16.75 16.75 16.75H3.25C2.44714 16.75 1.75017 16.0672 1.75 15.167V4.83301C1.75017 3.93278 2.44715 3.25 3.25 3.25Z" stroke="#CED0D6" stroke-width="1.5"/></svg>
- **Gradle**: By default run configs will try to use project's Gradle wrapper (gradlew). If you want to use some other
  Gradle binary, you can configure this here.
- **Maven path**: By default run configs will try to use project's Maven (mvn). If you want to use some other
  Maven binary, you can configure this here.
- **JDK path**: By default run configs won't explicitly set JDK path. If you think, that you need to use some concrete JDK
  version, you can set it here. (For gradle tasks will be specified `-Dorg.gradle.java.home=<path>` and for maven tasks)
  `-Djava.home=<path>`)
- **Anaconda path**: *You must provide this path in order to work with anaconda run configs* This path is the path to directory where all your conda environments are stored. Usually it is something like `/Users/user/miniconda3/` (you mustn't specify `/envs/` part)