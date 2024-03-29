package info.kgeorgiy.java.advanced.implementor.basic.classes.standard;

public class RMIServerImplImpl extends info.kgeorgiy.java.advanced.implementor.basic.classes.standard.RMIServerImpl {
    RMIServerImplImpl(java.util.Map arg0)  {
        super(arg0);
    }
    @Override
    protected void closeClient(javax.management.remote.rmi.RMIConnection arg0) {
        return ;
    }

    @Override
    protected void closeServer() {
        return ;
    }

    @Override
    protected void export() {
        return ;
    }

    @Override
    protected java.lang.String getProtocol() {
        return null;
    }

    @Override
    protected javax.management.remote.rmi.RMIConnection makeClient(java.lang.String arg0, javax.security.auth.Subject arg1) {
        return null;
    }

    @Override
    public java.rmi.Remote toStub() {
        return null;
    }

}
