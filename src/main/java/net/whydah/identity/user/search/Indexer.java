package net.whydah.identity.user.search;

import net.whydah.identity.user.identity.UserIdentity;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Indexer for adding users to the index.
 */
public class Indexer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Indexer.class);

    public static final String FIELD_FIRSTNAME = "firstname";
    public static final String FIELD_LASTNAME = "surname";
    public static final String FIELD_UID = "uid";
    public static final String FIELD_USERNAME = "username";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_PERSONREF = "personref";
    public static final String FIELD_MOBILE = "mobile";

    private final Directory index;
    private static final Analyzer ANALYZER = new StandardAnalyzer(Version.LUCENE_31);

    public Indexer(Directory index) {
        this.index = index;
    }

    public void removeFromIndex(String uid) {
        try {
            IndexWriter w = getWriter();
            w.deleteDocuments(new Term(FIELD_UID, uid));
            w.optimize();
            w.close();
        } catch (IOException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
    }

    public void update(UserIdentity user) {
        try {
            IndexWriter w = getWriter();
            w.updateDocument(new Term(FIELD_UID, user.getUid()), createLuceneDocument(user));
            w.optimize();
            w.close();
        } catch (IOException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
    }

    public void addToIndex(UserIdentity user) {
        try {
            IndexWriter writer = getWriter();
            addToIndex(writer, user);
            writer.optimize();
            writer.close();
        } catch (IOException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
    }

    public void addToIndex(List<UserIdentity> users) throws IOException {
        IndexWriter indexWriter = new IndexWriter(index, ANALYZER, IndexWriter.MaxFieldLength.UNLIMITED);
        for (UserIdentity user : users) {
            Document doc = createLuceneDocument(user);
            indexWriter.addDocument(doc);
        }
        indexWriter.optimize();
        indexWriter.close();
    }

    /**
     * Use with caution. Close writer after use.
     * @return IndexWriter
     * @throws org.apache.lucene.index.CorruptIndexException if the index is corrupt
     * @throws org.apache.lucene.store.LockObtainFailedException if another writer
     *  has this index open (<code>write.lock</code> could not
     *  be obtained)
     * @throws IOException if the directory cannot be
     *  read/written to or if there is any other low-level
     *  IO error
     */
    public IndexWriter getWriter() throws IOException {
        return new IndexWriter(index, ANALYZER, IndexWriter.MaxFieldLength.UNLIMITED);
    }

    public void addToIndex(IndexWriter writer, UserIdentity user) {
        try {
            Document doc = createLuceneDocument(user);
            writer.addDocument(doc);
        } catch (IOException e) {
            LOGGER.error("addToIndex failed for {}. Index was probably not updated.", user.toString(), e);
        }
    }

    private Document createLuceneDocument(UserIdentity user) {
        Document doc = new Document();
        doc.add(new Field(FIELD_FIRSTNAME, user.getFirstName(), Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field(FIELD_LASTNAME, user.getLastName(), Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field(FIELD_UID, user.getUid(), Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field(FIELD_USERNAME, user.getUsername(), Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field(FIELD_EMAIL, user.getEmail(), Field.Store.YES, Field.Index.ANALYZED));
        if (user.getPersonRef() != null) {
            doc.add(new Field(FIELD_PERSONREF, user.getPersonRef(), Field.Store.YES, Field.Index.NO));
        }

        if (user.getCellPhone() != null) {
            doc.add(new Field(FIELD_MOBILE, user.getCellPhone(), Field.Store.YES, Field.Index.ANALYZED));
        }
        return doc;
    }

}
