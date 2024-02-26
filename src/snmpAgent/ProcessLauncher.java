package snmpAgent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

public class ProcessLauncher extends Thread {

    private static final Logger LOG = Logger.getLogger(ProcessLauncher.class.getName());

    private final int timeoutInSeconds;

    private final String[] command;

    private Process process;


    public ProcessLauncher(String[] command, int timeoutInSeconds) {
        this.timeoutInSeconds = timeoutInSeconds > 0 ? timeoutInSeconds + 5 : 0;
        this.command = command;
        this.process = null;
    }

    @Override
    public void run() {

        try {

            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                runOnWindows();

            } else if (System.getProperty("os.name").toLowerCase().contains("linux")) {
                runOnLinux();

            } else {
                LOG.error("Unexpected System.getProperty [" + System.getProperty("os.name") + "]");
            }
        } catch (IllegalArgumentException | SecurityException ex) {
            LOG.error(ex);
        }
    }
    
    public void tryTokillProcessAndSubprocess() {

        try {

            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                throw new java.lang.UnsupportedOperationException("Not supported yet.");

            } else if (System.getProperty("os.name").toLowerCase().contains("linux")) {
                tryTokillProcessAndSubprocessOnLinux();

            } else {
                LOG.error("Unexpected System.getProperty [" + System.getProperty("os.name") + "]");
            }
        } catch (IllegalArgumentException | SecurityException ex) {
            LOG.error(ex);
        }
    }

    private void runOnLinux() {

        try {
            /* Exec process */
            process = Runtime.getRuntime().exec(command);
            Optional<Integer> pid = getPidOnLinux();
            
            /* print output  */
            logProcessOutput();

            /* Waiting until process is finished */
            if (timeoutInSeconds > 0 && pid.isPresent()) {
                boolean exited = process.waitFor(timeoutInSeconds, TimeUnit.SECONDS);

                if (!exited) {
                    boolean killed = killProcessAndSubprocessOnLinux(pid.get());

                    LOG.error("Se ha intentado finalizar el proceso " + Arrays.toString(command)
                            + " porque ha superado el tiempo esperado de ejecución. Killed ["
                            + killed + "] isAlive [" + process.isAlive() + "]");

                } else {
                    LOG.debug("Exit value: " + process.exitValue() + " CMD " + Arrays.toString(command));
                }

            } else {
                process.waitFor();
                LOG.error("Exit value: " + process.exitValue() + " CMD " + Arrays.toString(command));
            }

        } catch (Exception ex) {
            LOG.error(ex);
        }
    }

    private void runOnWindows() {

        try {
            String cmd = "";
            for (int i = 0; i < command.length; i++) {
                cmd += command[i] + " ";
            }

            LOG.debug("Se ejecuta el proceso: " + cmd);
            process = Runtime.getRuntime().exec("cmd /c " + cmd);

            //logProcessOutput();

            /* Waiting until process is finished */
            process.waitFor();
            LOG.debug("Exit value: " + process.exitValue() + " CMD " + Arrays.toString(command));

        } catch (IOException | IllegalArgumentException | SecurityException | InterruptedException e) {
            LOG.error(e);
        }
    }
    
    private boolean tryTokillProcessAndSubprocessOnLinux() {
        Optional<Integer> pid = getPidOnLinux();
        boolean killed = false;

        if (pid.isPresent()) {
            killed = killProcessAndSubprocessOnLinux(pid.get());

            LOG.info("Se ha intentado finalizar el proceso " + Arrays.toString(command)
                    + " antes de tiempo debido a un evento. Killed ["
                    + killed + "] isAlive [" + process.isAlive() + "]");

        } else {
            LOG.error("No se ha podido finalizar la ejecución porque no se ha podido obtener el pid");
        }

        return killed;
    }

    private boolean killProcessAndSubprocessOnLinux(int pid) {
        boolean killed = false;

        try {
            Process pkillProcess = Runtime.getRuntime().exec("pkill -9 -P " + pid);
            pkillProcess.waitFor();
            LOG.debug("Exit value: " + pkillProcess.exitValue() + " pkill -9 -P " + pid);
            killed = pkillProcess.exitValue() == 0;
            
            Process killProcess = Runtime.getRuntime().exec("kill -9 " + pid);
            killProcess.waitFor();
            LOG.debug("Exit value: " + killProcess.exitValue() + " kill -9 -P " + pid);

        } catch (IOException | InterruptedException ex) {
            LOG.error(ex);
        }

        return killed;
    }

    private Optional<Integer> getPidOnLinux() {
        Optional<Integer> pid = Optional.empty();

        try {
            Field field = process.getClass().getDeclaredField("pid");
            field.setAccessible(true);

            pid = Optional.of(field.getInt(process));
            LOG.debug("PID = " + field.get(process) + " CMD [" + Arrays.toString(command) + "]");

        } catch (NumberFormatException | NoSuchFieldException | IllegalAccessException ex) {
            LOG.error(" al obtener el pid del proceso, por lo que no se puede esperar por tiempo al proceso. \n " + ex);
        }
        return pid;
    }

    private void logProcessOutput() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader ebr = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String auxEr = ebr.readLine();

            while (auxEr != null) {
//                LOG.error(auxEr);
                auxEr = ebr.readLine();
            }

            String aux = br.readLine();

            while (aux != null) {
//                LOG.debug(aux);
                aux = br.readLine();
            }
        } catch (IOException ex) {
            LOG.error(ex);
        }
    }
}
