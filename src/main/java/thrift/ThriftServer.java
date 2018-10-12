package thrift;

import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import register.ZookeeperServer;
import server.IkoalasServer;
import server.KoalasDefaultThreadFactory;
import server.config.AbstractKoalsServerPublisher;
import server.config.ZookServerConfig;
import utils.KoalasThreadedSelectorWorkerExcutorUtil;

import java.util.concurrent.ExecutorService;

public class ThriftServer implements IkoalasServer {
    private final static Logger logger = LoggerFactory.getLogger ( ThriftServer.class );

    private AbstractKoalsServerPublisher serverPublisher;

    private TProcessor tProcessor;
    private TNonblockingServerSocket tServerTransport;
    private TServer tServer;

    private ExecutorService executorService;

    private ZookeeperServer zookeeperServer;


    public ThriftServer(AbstractKoalsServerPublisher serverPublisher) {
        this.serverPublisher = serverPublisher;
    }

    @Override
    public void run() {
         tProcessor = serverPublisher.getTProcessor ();
         if(tProcessor == null){
             logger.error ( "the tProcessor can't be null serverInfo={}",serverPublisher );
             throw new IllegalArgumentException("the tProcessor can't be null ");
         }
        try {
            tServerTransport = new TNonblockingServerSocket (serverPublisher.port);
            TThreadedSelectorServer.Args tArgs = new TThreadedSelectorServer.Args(tServerTransport);
            TFramedTransport.Factory transportFactory = new TFramedTransport.Factory();
            TProtocolFactory tProtocolFactory = new TBinaryProtocol.Factory();
            tArgs.transportFactory(transportFactory);
            tArgs.protocolFactory(tProtocolFactory);
            tArgs.processor (tProcessor);
            tArgs.selectorThreads ( serverPublisher.bossThreadCount==0?AbstractKoalsServerPublisher.DEFAULT_EVENT_LOOP_THREADS:serverPublisher.bossThreadCount );
            tArgs.workerThreads ( serverPublisher.workThreadCount==0? AbstractKoalsServerPublisher.DEFAULT_EVENT_LOOP_THREADS*2:serverPublisher.workThreadCount);
            executorService = KoalasThreadedSelectorWorkerExcutorUtil.getWorkerExcutor (serverPublisher.koalasThreadCount==0?AbstractKoalsServerPublisher.DEFAULT_KOALAS_THREADS:serverPublisher.koalasThreadCount,new KoalasDefaultThreadFactory (serverPublisher.serviceInterface.getName ()));
            tArgs.executorService (executorService);
            tArgs.acceptQueueSizePerThread(AbstractKoalsServerPublisher.DEFAULT_THRIFT_ACCETT_THREAD);
            tServer = new TThreadedSelectorServer(tArgs);
            Runtime.getRuntime().addShutdownHook(new Thread(){
                @Override
                public void run(){
                    if(tServer!= null && tServer.isServing ()){
                        tServer.stop ();
                    }
                    if(zookeeperServer != null){
                        zookeeperServer.destroy ();
                    }
                }
            });
            new Thread (new ThriftRunable(tServer) ).start ();

            if(StringUtils.isNotEmpty ( serverPublisher.zkpath )){
                ZookServerConfig zookServerConfig = new ZookServerConfig ( serverPublisher.zkpath,serverPublisher.serviceInterface.getName (),serverPublisher.env,serverPublisher.port,serverPublisher.weight,"thrift" );
                zookeeperServer = new ZookeeperServer ( zookServerConfig );
                zookeeperServer.init ();
            }

         } catch (TTransportException e) {
            logger.error ( "the tProcessor can't be null serverInfo={}",serverPublisher );
            throw new IllegalArgumentException("the tProcessor can't be null");
        }
        logger.info("thrift server init success server={}",serverPublisher);
    }

    @Override
    public void stop() {

        if(executorService!=null){
            executorService.shutdown ();
        }

        if(tServer!= null && tServer.isServing ()){
            tServer.stop ();
        }
        zookeeperServer.destroy ();
        logger.info("thrift server stop success server={}",serverPublisher);

    }

    private class ThriftRunable implements Runnable {

        private TServer tServer;

        public ThriftRunable(TServer tServer) {
            this.tServer = tServer;
        }

        @Override
        public void run() {
            tServer.serve ();
        }
    }
}
