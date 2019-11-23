package com.sbt.lesson;

import javax.tools.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

/**
 * Сделать свой класслоадер который из имеющегося списка путей на диске C:\ (2шт) ищет  класс SayHello
 * с методом sayHello().
 * К примеру в одном каталоге есть SayHello#say() в другом каталоге класс SayHello#sayHello()
 * Фактически необходима обертка в виде своего класслоадера над кодом в этом примере.
 * Свой класслоадер, перебрав имеющиеся классы, выбирает правильный и выдает его.
 *
 */
public class ModifyMain {

    /*private static final String DIR_NAME_OLD = System.getProperty("java.io.tmpdir") + "old";
    private static final String DIR_NAME_NEW = System.getProperty("java.io.tmpdir") + "new";*/
    //URL classUrl = new FilePath().getClass().getResource("ModifyMain");
    private static final String DIR_NAME_OLD = System.getProperty("user.dir") + "\\out\\production\\Lesson7_1\\com\\sbt\\lesson\\old";
    private static final String DIR_NAME_NEW = System.getProperty("user.dir") + "\\out\\production\\Lesson7_1\\com\\sbt\\lesson\\new";
    private static final String CLASS_NAME = "SayHello";
    public static final String METHOD_NAME_INCORRECT = "say";
    public static final String METHOD_NAME_CORRECT = "sayHello";

    public static void main(String[] args) throws MalformedURLException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, ClassNotFoundException {
        //ModifyMain main = new ModifyMain();
        System.out.println(System.getProperty("user.dir"));
        /*main.checkDirectory(DIR_NAME_OLD);
        main.checkDirectory(DIR_NAME_NEW);

        try {
            Class<?> aClass = Class.forName(CLASS_NAME);
            System.out.println(aClass.getName());
        } catch (ClassNotFoundException e) {
            System.out.println("Класс " + CLASS_NAME + " отсутсвует в ClassPath  ");
        }

        //main.generateClass(DIR_NAME_OLD, CLASS_NAME, METHOD_NAME_INCORRECT);

        URLClassLoader classLoaderByDir1 = main.getClassLoaderByDir(DIR_NAME_OLD);

        Class<?> clazzFirst = loadClass(classLoaderByDir1, CLASS_NAME);

        if(main.searchMethod(clazzFirst, METHOD_NAME_CORRECT)){
            System.exit(0);
        }

        //main.generateClass(DIR_NAME_NEW, CLASS_NAME, METHOD_NAME_CORRECT);

        URLClassLoader classLoaderByDir2 = main.getClassLoaderByDir(DIR_NAME_NEW);*/
        Class<?> clazz ;//= loadClass(classLoaderByDir2, CLASS_NAME);

        clazz = myClassLoaderByDir(new String[]{DIR_NAME_OLD, DIR_NAME_NEW},CLASS_NAME,METHOD_NAME_CORRECT);

        if(clazz!=null){
            Method method = clazz.getDeclaredMethod(METHOD_NAME_CORRECT);
            System.out.println("Вызываем метод " + method.getName() + " из загруженного класса " + clazz.getName());
            method.invoke(clazz.newInstance());
        }
    }

    public static Class<?> myClassLoaderByDir(String[] directoryName, String className, String methodName) throws MalformedURLException {
        try {
            Class<?> aClass = Class.forName(className);
            System.out.println(aClass.getName());
            return aClass;
        } catch (ClassNotFoundException e) {
            System.out.println("Класс " + className + " отсутсвует в ClassPath  ");
        }

        //ModifyMain main = new ModifyMain();
        URLClassLoader classLoaderByDir;
        Class<?> clazzTest;
        for (int i = 0; i < directoryName.length; i++) {


            classLoaderByDir = getClassLoaderByDir(directoryName[i]);

            clazzTest = loadClass(classLoaderByDir, className);

            if(searchMethod(clazzTest, methodName)){
                return clazzTest;
                //System.exit(0);
            }

        }
        return null;
    }

    private static void checkDirectory(String dirName) {
        File directory = new File(dirName);
        if (! directory.exists()){
            directory.mkdir();
        }
    }

    private static Class<?> loadClass(URLClassLoader classLoader, String clazzName) {
        Class<?> clazzSecond = null;
        try {
            clazzSecond = classLoader.loadClass(clazzName);
            System.out.println("Из " + Arrays.asList(classLoader.getURLs()).get(0).getPath() + " загрузили класс " + clazzSecond.getName() +"\n");
        } catch (ClassNotFoundException e) {
            System.out.println("Не удалось загрузить класс " + clazzName + " из класслоадера " + classLoader.toString());
        }
        return clazzSecond;
    }

    private void generateClass(String dirName, String clazzName, String methodName){
        try {
            File tempFile = new File (dirName, clazzName +".java");
            tempFile.deleteOnExit();
            String className = tempFile.getName().split("\\.")[0];
            String sourceCode = "public class " + className + " { public void "+ methodName + "() { System.out.println(\"Hello everybody !\"); } }";
            FileWriter fileWriter = new FileWriter(tempFile);
            fileWriter.write(sourceCode);
            fileWriter.close();

            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            StandardJavaFileManager manager = compiler.getStandardFileManager(null, null, null);

            File parentDirectory = tempFile.getParentFile();
            manager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(parentDirectory));
            Iterable<? extends JavaFileObject> compilationUnits = manager.getJavaFileObjectsFromFiles(Arrays.asList(tempFile));
            compiler.getTask(null, manager, null, null, null, compilationUnits).call();
            manager.close();
            System.out.println("Сгенерирован класс " + tempFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean searchMethod(Class<?> clazz, String methodName) {
        try {
            Method say = clazz.getMethod(methodName);
            System.out.println("В классе " + clazz.getName()  + " загрузчика " + clazz.getClassLoader() + " найден метод " + say.getName());
            return true;
        } catch (NoSuchMethodException e) {
            System.out.println("В классе " + clazz.getName() + " загрузчика " + clazz.getClassLoader() + " метод " + methodName + " не найден!");
        }
        return false;
    }

    public static URLClassLoader getClassLoaderByDir(String directoryName) throws MalformedURLException {
        File dir = new File(directoryName);
        URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{dir.toURI().toURL()});
        return urlClassLoader;
    }
}
