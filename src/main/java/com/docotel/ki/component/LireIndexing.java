package com.docotel.ki.component;

import com.docotel.ki.util.NumberUtil;
import lire.IndexingThread;
import net.semanticmetadata.lire.indexing.LireCustomCodec;
import net.semanticmetadata.lire.utils.LuceneUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class LireIndexing {
	private static boolean indexingOnProgress = false;
	private static double indexingProgress = 0.0;
	private static List<String> pendingIndexTargetList = new ArrayList<>();

	private static String indexSimilarityFilePath;

	public LireIndexing(@Value("${index.similarity.file.path:}") String indexSimilarityFilePath) {
		LireIndexing.indexSimilarityFilePath = indexSimilarityFilePath;
		File file = new File(LireIndexing.indexSimilarityFilePath);
		if (file.exists()) {
			try {
				File[] indexFiles = file.listFiles();
				for (File indexFile : indexFiles) {
					indexFile.delete();
				}
			} catch (NullPointerException e) {
			}
		}
	}

	public synchronized static void setIndexingProgress(int overallCount, int numImages) {
		LireIndexing.indexingProgress = ((double) overallCount / (double) numImages) * 100;
	}

	public String getIndexingProgress() {
		return NumberUtil.formatDecimal(indexingProgress);
	}

	public synchronized void startIndexing(String indexTarget) {
		pendingIndexTargetList.addAll(getAllImages(new File(indexTarget), true));

		if (indexingOnProgress) {
			return;
		}

		finishIndexing();
	}

	private static List<String> getAllImages(File directory, boolean descendIntoSubDirectories) {
		ArrayList<String> resultList = new ArrayList<String>(256);
		if (directory.isDirectory()) {
			File[] f = directory.listFiles();
			for (File file : f) {
				addFileToIndex(file, resultList);
				if (descendIntoSubDirectories && file.isDirectory()) {
					List<String> tmp = getAllImages(file, true);
					if (tmp != null) {
						resultList.addAll(tmp);
					}
				}
			}
		} else if (directory.isFile()){
			addFileToIndex(directory, resultList);
		}
		return resultList;
	}

	private static void addFileToIndex(File file, List<String> resultList) {
		if (file != null && (file.getName().toLowerCase().endsWith(".jpg") || file.getName().toLowerCase().endsWith(".jpeg"))) {
			try {
				resultList.add(file.getCanonicalPath());
			} catch (IOException e) {
			}
		};
	}

	public synchronized static void finishIndexing() {
		indexingProgress = 0.0;
		if (pendingIndexTargetList.size() > 0) {
			indexingOnProgress = true;
			List<String> targetList = new ArrayList<>();
			targetList.addAll(pendingIndexTargetList);
			pendingIndexTargetList.clear();

			IndexWriterConfig config = new IndexWriterConfig(LuceneUtils.LUCENE_VERSION, new StandardAnalyzer(LuceneUtils.LUCENE_VERSION));
			config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			config.setCodec(new LireCustomCodec());

			List<Term> termList = new ArrayList<>(targetList.size());
			for (String imagePath : targetList) {
				termList.add(new Term("descriptorImageIdentifier", imagePath));
			}

			try {
				IndexWriter writer = new IndexWriter(FSDirectory.open(new File(indexSimilarityFilePath)), config);
				writer.deleteDocuments(termList.toArray(new Term[] {}));
				writer.commit();
				writer.forceMerge(1);
				writer.close();
			} catch (IOException e) {
			}
			new IndexingThread(indexSimilarityFilePath, targetList).start();
		} else {
			indexingOnProgress = false;
			indexingProgress = 100;
		}
	}

	public boolean isIndexingOnProgress() {
		return indexingOnProgress;
	}
}
