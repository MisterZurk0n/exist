package org.exist.storage.btree;

import org.exist.EXistException;
import org.exist.indexing.Index;
import org.exist.indexing.StructuralIndex;
import org.exist.storage.BrokerPool;
import org.exist.storage.DBBroker;
import org.exist.storage.NativeBroker;
import org.exist.storage.lock.LockManager;
import org.exist.storage.lock.ManagedLock;
import org.exist.storage.structural.NativeStructuralIndexWorker;
import org.exist.util.Configuration;
import org.exist.util.DatabaseConfigurationException;
import org.exist.util.FileUtils;

import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Utility to rebuild any of the b+-tree based index files. Scans through all leaf pages to
 * reconstruct the inner b+-tree.
 */
public class Repair {

    private BrokerPool pool;

    public Repair() {
        startDB();
    }

    public void repair(String id) {
        try(final DBBroker broker = pool.get(Optional.of(pool.getSecurityManager().getSystemSubject()))) {

            BTree btree = null;
            if ("collections".equals(id)) {
                btree = ((NativeBroker)broker).getStorage(NativeBroker.COLLECTIONS_DBX_ID);
            } else if ("dom".equals(id)) {
                btree = ((NativeBroker)broker).getStorage(NativeBroker.DOM_DBX_ID);
            } else if ("range".equals(id)) {
                btree = ((NativeBroker)broker).getStorage(NativeBroker.VALUES_DBX_ID);
            } else if ("structure".equals(id)) {
                NativeStructuralIndexWorker index = (NativeStructuralIndexWorker)
                        broker.getIndexController().getWorkerByIndexName(StructuralIndex.STRUCTURAL_INDEX_ID);
                btree = index.getStorage();
            } else {
                // use index id defined in conf.xml
                Index index = pool.getIndexManager().getIndexByName(id);
                if (index != null) {
                    btree = index.getStorage();
                }
            }
            if (btree == null) {
                System.console().printf("Unkown index: %s\n", id);
                return;
            }

            final LockManager lockManager = broker.getBrokerPool().getLockManager();
            try(final ManagedLock<ReentrantLock> btreeLock = lockManager.acquireBtreeWriteLock(btree.getLockName())) {
                System.console().printf("Rebuilding %15s ...", FileUtils.fileName(btree.getFile()));
                btree.rebuild();
                System.out.println("Done");
            }

        } catch (Exception e) {
            System.console().printf("An exception occurred during repair: %s\n", e.getMessage());
            e.printStackTrace();
        }
    }

    private void startDB() {
        try {
            Configuration config = new Configuration();

            BrokerPool.configure(1, 5, config);
            pool = BrokerPool.getInstance();
        } catch (DatabaseConfigurationException | EXistException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        pool.shutdown(false);
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("\nUsage: " + Repair.class.getName() + " [index-name]+\n");
            System.out.println("Rebuilds the index files specified as arguments. Can be applied to");
            System.out.println("any of the b+-tree based indexes: collections, dom, structure, ngram-index.");
            System.out.println("The b+-tree is rebuild by scanning all leaf pages in the .dbx file.");
            System.out.println("Crash recovery uses the same operation.\n");
            System.out.println("Example call to rebuild all indexes:\n");
            System.out.println(Repair.class.getName() + " dom collections structure ngram-index");
        } else {
            Repair repair = new Repair();
            for (String arg: args) {
                repair.repair(arg);
            }
            repair.shutdown();
        }
    }
}
