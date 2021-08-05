package generic;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
/**
 * Copyright (C) 2018
 * All rights reserved
 * User: yulong.zhang
 * Date:2019年05月09日12:24:59
 */
public class GenericServiceImpl implements GenericService.Iface {
    private final static Logger logger = LoggerFactory.getLogger ( GenericServiceImpl.class );

    private Object realImpl;
    private String realClassName;
    public GenericServiceImpl(Object realImpl) {
        this.realImpl = realImpl;
    }

    @Override
    public String invoke(GenericRequest request) throws TException {
        //"getRpc"
        String methodName = request.getMethodName ();
        //["java.lang.String","java.lang.Long"]
        List<String> classTypes = request.getClassType ();
        //["4","5"]
        List<String> requestObjs = request.getRequestObj ();
        Class<?> targetImpl = AopUtils.getTargetClass ( realImpl );
        Class<?> ifaceClass = getIfaceClass ( targetImpl );
        if (ifaceClass == null) {
            throw new TException ("class:" + targetImpl + " not found in the iface service!" );

        }
        this.realClassName = targetImpl.getName ();
        checkparam(request);

        List<Class> realClassTypes = null;

        if(classTypes!=null && classTypes.size ()>0){
            realClassTypes = new ArrayList<> (  );
            for(String _classTypes:classTypes){
                try {
                    realClassTypes.add (getClassType(_classTypes));
                } catch (ClassNotFoundException e) {
                    throw new TException ("class:" + realClassName +  ",classType:"+_classTypes+ " not found in the server side!" );
                }
            }
        }

        Method method=null;
        List<Object> realRequest = null;
        if(realClassTypes==null || realClassTypes.size ()==0){
            try {
                method = ifaceClass.getMethod ( methodName );
            } catch (NoSuchMethodException e) {
                throw new TException ( "class:" + realClassName +  ",method:"+methodName+ " not found !" );
            }
        } else{
            try {
                method = ifaceClass.getMethod ( methodName ,realClassTypes.toArray ( new Class[0] ));
            } catch (NoSuchMethodException e) {
                throw new TException ( "class:" + realClassName +  ",method:"+methodName+ " not found !" );
            }
            realRequest = new ArrayList<> (  );

            for(int i =0;i<requestObjs.size ();i++){
                try {
                   Object o= JSONObject.parseObject (requestObjs.get ( i ),realClassTypes.get ( i ));
                   realRequest.add ( o );
                } catch (Exception e) {
                    throw new TException ( "class:" + realClassName +  ",method:"+methodName+ " ,JSONObject.parseObject error, ! text:"+ requestObjs.get ( i ) + ",classType:" + realClassTypes.get ( i ) );
                }
            }
        }

        Object ojb =null;
        if(realClassTypes==null || realClassTypes.size ()==0){
            try {
                ojb = method.invoke ( realImpl );
            } catch (Exception e) {
                Throwable cause = e.getCause ()!=null?e.getCause ():e;
                throw new TException (cause.getMessage ());
            }
        }else{
            try {
                ojb = method.invoke ( realImpl,realRequest.toArray ( new Object[0] ) );
            } catch (Exception e) {
                Throwable cause = e.getCause ()!=null?e.getCause ():e;
                throw new TException (cause);
            }
        }

        if(ojb==null){
            return "VOID";
        } else{
            return JSON.toJSONString ( ojb );
        }
    }

    private Class<?> getClassType(String stringType) throws ClassNotFoundException {
        switch (stringType){
            case "int":
                return int.class;
            case "double":
                return double.class;
            case "long":
                return long.class;
            case "short":
                return short.class;
            case "byte":
                return byte.class;
            case "boolean":
                return boolean.class;
            case "char":
                return char.class;
            case "float":
                return float.class;
            default:
                return this.getClass ().getClassLoader ().loadClass ( stringType );
        }
    }

    private void checkparam(GenericRequest request) throws TException {
        String methodName = request.getMethodName ();
        List<String> classTypes = request.getClassType ();
        List<String> requestObjs = request.getRequestObj ();

        if(classTypes != null && requestObjs == null){
            throw new TException ( "class:" + realClassName +  ",generic param check error classTypes != null && requestObjs == null " );
        }
        if(classTypes == null && requestObjs != null){
            throw new TException ( "class:" + realClassName +  ",generic param check error classTypes == null && requestObjs != null" );
        }
        if (classTypes != null  && (classTypes.size () != requestObjs.size ())) {
            throw new TException ( "class:" + realClassName +  ",generic param check error list size error" );
        }
        if(StringUtils.isEmpty (  methodName)){
            throw new TException ( "class:" + realClassName +  ",methodName can not be empty!" );
        }
    }

    public Class getIfaceClass(Class classImpl){
        while (classImpl != Object.class){
            Class[] interfaces = classImpl.getInterfaces ();

            if(interfaces!= null){
                for(Class iface:interfaces){
                    if(iface.getName ().contains ( "$Iface" )){
                        return iface;
                    }
                }
            }
            classImpl = classImpl.getSuperclass ();
        }
        return null;
    }

}
