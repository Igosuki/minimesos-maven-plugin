package com.containersolutions.minimesos;

import com.containersol.minimesos.cluster.ClusterRepository;
import com.containersol.minimesos.cluster.MesosCluster;
import com.containersol.minimesos.mesos.MesosClusterContainersFactory;
import org.apache.maven.plugin.AbstractMojo;

import java.util.logging.Logger;

public abstract class AbstractMinimesosMojo extends AbstractMojo {
    private static final Logger LOGGER = Logger.getAnonymousLogger();

    public static final String MESOS_CLUSTER_KEY = "mesos_cluster";
    protected final ClusterRepository repository = new ClusterRepository();

    public AbstractMinimesosMojo() {
    }

    protected void startMinimesos(MesosCluster cluster) {
        destroyMinimesos(); // Kill previous minimesos if running
        repository.saveClusterFile(cluster);
        getLog().info("Starting minimesos");
        cluster.start();
        cluster.waitForState(state -> state != null);
        getLog().info("Started minimesos");
    }

    protected void destroyMinimesos() {
        MesosClusterContainersFactory clusterFactory = new MesosClusterContainersFactory();
        try {
            MesosCluster cluster = repository.loadCluster(clusterFactory);
            if (cluster != null) {
                cluster.destroy(clusterFactory);
                LOGGER.info("Destroyed minimesos cluster with ID " + cluster.getClusterId());
            } else {
                LOGGER.info("Minimesos cluster is not running");
            }
        } catch(Exception e) {
            LOGGER.info("Failed to destroy cluster, perhaps it is not running");
        }
    }
}
