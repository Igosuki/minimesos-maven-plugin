package com.containersolutions.minimesos;

import com.containersol.minimesos.MinimesosException;
import com.containersol.minimesos.cluster.MesosCluster;
import com.containersol.minimesos.config.ClusterConfig;
import com.containersol.minimesos.config.ConfigParser;
import com.containersol.minimesos.mesos.MesosClusterContainersFactory;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.stream.Collectors;

/**
 * Start the minimesos cluster
 */
@Mojo( name = "start", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST)
public class StartMinimesos extends AbstractMinimesosMojo
{
    /**
     * A relative or absolute path to a minimesos configuration file (a.k.a minimesosFile)
     */
    @Parameter(property = "configFile")
    private File configFile;

    @Parameter(defaultValue = "${project}", required = true, readonly = false)
    MavenProject project;

    public void execute() throws MojoExecutionException
    {
        MesosClusterContainersFactory mesosClusterFactory = new MesosClusterContainersFactory();
        MesosCluster cluster = null;
        if (configFile == null) {
            getLog().info("Using default configuration");
            ClusterConfig config = new ClusterConfig();
            cluster = mesosClusterFactory.createMesosCluster(config);
        } else {
            getLog().info("Loading configuration file...");
            cluster = mesosClusterFactory.createMesosCluster(parseConfigFile());
        }

        startMinimesos(cluster);
        writeProperties(cluster);
    }

    private void writeProperties(MesosCluster mesosCluster) {
        getLog().info("Writing properties");
        project.getProperties().setProperty("zookeeper_ip", mesosCluster.getZooKeeper().getIpAddress());
        project.getProperties().setProperty("mesos_master_ip", mesosCluster.getMaster().getIpAddress());
        getLog().debug(project.getProperties().entrySet().stream().map(entry -> entry.getKey().toString() + "=" + entry.getValue().toString()).collect(Collectors.joining("\n")));
    }

    private ClusterConfig parseConfigFile() {
        if (configFile != null) {
            ConfigParser configParser = new ConfigParser();
            try {
                return configParser.parse(FileUtils.readFileToString(configFile));
            } catch (Exception e) {
                String msg = String.format("Failed to load cluster configuration from %s: %s", configFile, e.getMessage());
                throw new MinimesosException(msg, e);
            }
        }
        throw new MinimesosException("No minimesosFile found in current directory. Please generate one with 'minimesos init'");
    }

}