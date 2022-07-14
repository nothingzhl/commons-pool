package org.apache.commons.pool2;

import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.jupiter.api.Test;

/**
 * @author zhanghanlin
 * @date 2022/7/14
 **/
public class CommonTest {
    @Test
    public void test() throws Exception {

        final GenericObjectPoolConfig<StringBuffer> config = new GenericObjectPoolConfig<>();

        config.setMaxTotal(8);
        config.setMaxIdle(4);
        config.setMinIdle(2);

        GenericObjectPool<StringBuffer> pool = new GenericObjectPool<>(new BasePooledObjectFactory<StringBuffer>() {
            @Override
            public StringBuffer create() {
                return new StringBuffer();
            }

            @Override
            public PooledObject<StringBuffer> wrap(StringBuffer buffer) {
                return new DefaultPooledObject<>(buffer);
            }

            @Override
            public void passivateObject(PooledObject<StringBuffer> pooledObject) {
                pooledObject.getObject().setLength(0);
            }
        }, config);

        printPoolStatus(pool);
        System.out.println("pool 初始化完成\n");
        StringBuffer[] buffer = new StringBuffer[9];
        for (int i = 0; i < 8; i++) {
            System.out.print("borrow " + i);
            buffer[i] = pool.borrowObject();
            buffer[i].append("string buffer " + i);
            printPoolStatus(pool);
        }
        // 如果 borrow 第 9 个，会阻塞
        //        buffer[8] = pool.borrowObject();
        for (int i = 0; i < 8; i++) {
            System.out.print("return " + i);
            pool.returnObject(buffer[i]);
            printPoolStatus(pool);
        }
    }

    private void printPoolStatus(GenericObjectPool pool) {
        System.out
            .printf("[active:%s,idle:%s,wait:%s]%n", pool.getNumActive(), pool.getNumIdle(), pool.getNumWaiters());
    }

}
