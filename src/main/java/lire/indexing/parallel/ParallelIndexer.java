package lire.indexing.parallel;

import com.docotel.ki.component.LireIndexing;
import net.semanticmetadata.lire.DocumentBuilderFactory;
import net.semanticmetadata.lire.impl.ChainedDocumentBuilder;
import net.semanticmetadata.lire.indexing.LireCustomCodec;
import net.semanticmetadata.lire.indexing.parallel.WorkItem;
import net.semanticmetadata.lire.utils.LuceneUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class ParallelIndexer implements Runnable {
	@Autowired
	private Environment env;

	private Logger log = Logger.getLogger(this.getClass().getName());
	private int numberOfThreads = 10;
	private String indexPath;
	IndexWriter writer;
	boolean ended = false;
	boolean threadFinished = false;
	private List<String> files;
	int overallCount = 0;
	int numImages = -1;
	private OpenMode openMode;
	private int monitoringInterval;
	private LinkedBlockingQueue<WorkItem> queue;

	public ParallelIndexer(int numberOfThreads, String indexPath, List<String> images) {
		this.openMode = OpenMode.CREATE_OR_APPEND;
		this.monitoringInterval = 30;
		this.queue = new LinkedBlockingQueue(100);
		this.numberOfThreads = numberOfThreads;
		this.indexPath = indexPath;
		this.files = images;
	}

	public void addBuilders(ChainedDocumentBuilder builder) {
		builder.addBuilder(DocumentBuilderFactory.getCEDDDocumentBuilder());
		builder.addBuilder(DocumentBuilderFactory.getFCTHDocumentBuilder());
		builder.addBuilder(DocumentBuilderFactory.getJCDDocumentBuilder());
		builder.addBuilder(DocumentBuilderFactory.getPHOGDocumentBuilder());
		builder.addBuilder(DocumentBuilderFactory.getOpponentHistogramDocumentBuilder());
		builder.addBuilder(DocumentBuilderFactory.getJointHistogramDocumentBuilder());
		builder.addBuilder(DocumentBuilderFactory.getColorLayoutBuilder());
		builder.addBuilder(DocumentBuilderFactory.getEdgeHistogramBuilder());
	}

	public void run() {
		IndexWriterConfig config = new IndexWriterConfig(LuceneUtils.LUCENE_VERSION, new StandardAnalyzer(LuceneUtils.LUCENE_VERSION));
		config.setOpenMode(this.openMode);
		config.setCodec(new LireCustomCodec());

		try {
			this.writer = new IndexWriter(FSDirectory.open(new File(this.indexPath)), config);
			this.numImages = this.files.size();
//			System.out.println("Indexing " + this.files.size() + " images.");
			Thread p = new Thread(new lire.indexing.parallel.ParallelIndexer.Producer(this.files));
			p.start();
			LinkedList<Thread> threads = new LinkedList();
//			long l = System.currentTimeMillis();

			for(int i = 0; i < this.numberOfThreads; ++i) {
				Thread c = new Thread(new lire.indexing.parallel.ParallelIndexer.Consumer());
				c.start();
				threads.add(c);
			}

			Thread m = new Thread(new lire.indexing.parallel.ParallelIndexer.Monitoring());
			m.start();
			Iterator iterator = threads.iterator();

			while(iterator.hasNext()) {
				((Thread)iterator.next()).join();
			}

//			long l1 = System.currentTimeMillis() - l;
//			int seconds = (int)(l1 / 1000L);
//			int minutes = seconds / 60;
//			seconds %= 60;
//			System.out.printf("Analyzed %d images in %03d:%02d ~ %3.2f ms each.\n", this.overallCount, minutes, seconds, this.overallCount > 0 ? (float)l1 / (float)this.overallCount : -1.0F);

		} catch (IOException var11) {
			var11.printStackTrace();
		} catch (InterruptedException var12) {
			var12.printStackTrace();
		} finally {
			if (this.writer != null) {
				try {
					this.writer.commit();
					this.writer.forceMerge(1);
					this.writer.close();
				} catch (IOException var11) {
					var11.printStackTrace();
				}
				this.threadFinished = true;
				LireIndexing.finishIndexing();
			}
		}
	}

	class Consumer implements Runnable {
		WorkItem tmp = null;
		ChainedDocumentBuilder builder = new ChainedDocumentBuilder();
		int count = 0;
		boolean locallyEnded = false;

		Consumer() {
			lire.indexing.parallel.ParallelIndexer.this.addBuilders(this.builder);
		}

		public void run() {
			while(!this.locallyEnded) {
				this.tmp = null;

				try {
					this.tmp = lire.indexing.parallel.ParallelIndexer.this.queue.take();
					if (this.tmp.getFileName() == null) {
						this.locallyEnded = true;
					} else {
						++this.count;
						++lire.indexing.parallel.ParallelIndexer.this.overallCount;
						LireIndexing.setIndexingProgress(lire.indexing.parallel.ParallelIndexer.this.overallCount, lire.indexing.parallel.ParallelIndexer.this.numImages);
					}
				} catch (InterruptedException var5) {
					lire.indexing.parallel.ParallelIndexer.this.log.severe(var5.getMessage());
				}

				try {
					if (!this.locallyEnded && this.tmp != null) {
						ByteArrayInputStream b = new ByteArrayInputStream(this.tmp.getBuffer());
						BufferedImage img = ImageIO.read(b);
						Document d = this.builder.createDocument(img, this.tmp.getFileName());
						lire.indexing.parallel.ParallelIndexer.this.writer.addDocument(d);
					}
				} catch (IOException var4) {
					lire.indexing.parallel.ParallelIndexer.this.log.severe(var4.getMessage());
				}
			}

		}
	}

	class Producer implements Runnable {
		private List<String> files;
		Producer(List<String> files) {
			this.files = files;
		}

		public void run() {
			Iterator iterator = files.iterator();

			String s;
			File b;
			while(iterator.hasNext()) {
				s = (String)iterator.next();
				b = new File(s);

				try {
					byte[] buffer = Files.readAllBytes(Paths.get(s));
					s = b.getCanonicalPath();
					lire.indexing.parallel.ParallelIndexer.this.queue.put(new WorkItem(s, buffer));
				} catch (Exception var8) {
					System.err.println("Could not open " + s + ". " + var8.getMessage());
				}
			}

			for(int i = 0; i < lire.indexing.parallel.ParallelIndexer.this.numberOfThreads * 3; ++i) {
				s = null;

				try {
					lire.indexing.parallel.ParallelIndexer.this.queue.put(new WorkItem(s, (byte[]) null));
				} catch (InterruptedException var7) {
					var7.printStackTrace();
				}
			}

			lire.indexing.parallel.ParallelIndexer.this.ended = true;
		}
	}

	class Monitoring implements Runnable {
		Monitoring() {
		}

		public void run() {
			long ms = System.currentTimeMillis();

			try {
				Thread.sleep((long)(1000 * lire.indexing.parallel.ParallelIndexer.this.monitoringInterval));
			} catch (InterruptedException var8) {
				var8.printStackTrace();
			}

			while(!lire.indexing.parallel.ParallelIndexer.this.ended) {
				try {
					long time = System.currentTimeMillis() - ms;
					int seconds = (int)(time / 1000L);
					int minutes = seconds / 60;
					seconds %= 60;
					System.out.printf("Analyzed from %s %d images in %03d:%02d ~ %3.2f ms each. (queue size is %d)\n", env.getProperty("app.mode"), lire.indexing.parallel.ParallelIndexer.this.overallCount, minutes, seconds, lire.indexing.parallel.ParallelIndexer.this.overallCount > 0 ? (float)time / (float) lire.indexing.parallel.ParallelIndexer.this.overallCount : -1.0F, lire.indexing.parallel.ParallelIndexer.this.queue.size());
					Thread.sleep((long)(1000 * lire.indexing.parallel.ParallelIndexer.this.monitoringInterval));
				} catch (InterruptedException var7) {
					var7.printStackTrace();
				}
			}

		}
	}
}
