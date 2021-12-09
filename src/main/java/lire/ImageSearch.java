package lire;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.ImageSearcher;
import net.semanticmetadata.lire.ImageSearcherFactory;
import net.semanticmetadata.lire.impl.VisualWordsImageSearcher;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import javax.imageio.ImageIO;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ImageSearch {
	public static final int COLOR_LAYOUT_IMAGE_SEARCHER = 0;
	public static final int SCALABLE_COLOR_IMAGE_SEARCHER = 1;
	public static final int EDGE_HISTOGRAM_IMAGE_SEARCHER = 2;
	public static final int AUTO_COLOR_CORRELOGRAM_IMAGE_SEARCHER = 3;
	public static final int CEDD_IMAGE_SEARCHER = 4;
	public static final int FCTH_IMAGE_SEARCHER = 5;
	public static final int JCD_IMAGE_SEARCHER = 6;
	public static final int COLOR_HISTOGRAM_IMAGE_SEARCHER = 7;
	public static final int TAMURA_IMAGE_SEARCHER = 8;
	public static final int GABOR_IMAGE_SEARCHER = 9;
	public static final int JPEG_COEFFICIENT_HISTOGRAM_IMAGE_SEARCHER = 10;
	public static final int VISUAL_WORDS_IMAGE_SEARCHER = 11;
	public static final int JOINT_HISTOGRAM_IMAGE_SEARCHER = 12;
	public static final int OPPONENT_HISTOGRAM_IMAGE_SEARCHER = 13;
	public static final int LUMINANCE_LAYOUT_IMAGE_SEARCHER = 14;
	public static final int PHOG_IMAGE_SEARCHER = 15;

	private String indexPath;

	public ImageSearch(String indexPath) {
		this.indexPath = indexPath;
	}

	public List<ImageSearchResult> searchForImage(String imagePath, int numResults) throws FileNotFoundException {
		return searchForImage(new FileInputStream(imagePath), numResults);
	}

	public List<ImageSearchResult> searchForImage(InputStream searchedImageStream, int numResults) {
		return searchForImage(searchedImageStream, numResults, 0);
	}

	public List<ImageSearchResult> searchForImage(String imagePath, int numResults, int searchMethod) throws FileNotFoundException {
		return searchForImage(new FileInputStream(imagePath), numResults, searchMethod);
	}

	public List<ImageSearchResult> searchForImage(InputStream searchedImageStream, int numResults, int searchMethod) {
		List<ImageSearchResult> similarImageFilePathList = new ArrayList<>(numResults) ;
		final String indexPath = this.indexPath;
		try {
			IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
			ImageSearcher searcher = null;
			switch (searchMethod) {
				case COLOR_LAYOUT_IMAGE_SEARCHER:
					searcher = ImageSearcherFactory.createColorLayoutImageSearcher(numResults);
					break;
				case SCALABLE_COLOR_IMAGE_SEARCHER:
					searcher = ImageSearcherFactory.createScalableColorImageSearcher(numResults);
					break;
				case EDGE_HISTOGRAM_IMAGE_SEARCHER:
					searcher = ImageSearcherFactory.createEdgeHistogramImageSearcher(numResults);
					break;
				case AUTO_COLOR_CORRELOGRAM_IMAGE_SEARCHER:
					searcher = ImageSearcherFactory.createAutoColorCorrelogramImageSearcher(numResults);
					break;
				case CEDD_IMAGE_SEARCHER:
					searcher = ImageSearcherFactory.createCEDDImageSearcher(numResults);
					break;
				case FCTH_IMAGE_SEARCHER:
					searcher = ImageSearcherFactory.createFCTHImageSearcher(numResults);
					break;
				case JCD_IMAGE_SEARCHER:
					searcher = ImageSearcherFactory.createJCDImageSearcher(numResults);
					break;
				case COLOR_HISTOGRAM_IMAGE_SEARCHER:
					searcher = ImageSearcherFactory.createColorHistogramImageSearcher(numResults);
					break;
				case TAMURA_IMAGE_SEARCHER:
					searcher = ImageSearcherFactory.createTamuraImageSearcher(numResults);
					break;
				case GABOR_IMAGE_SEARCHER:
					searcher = ImageSearcherFactory.createGaborImageSearcher(numResults);
					break;
				case JPEG_COEFFICIENT_HISTOGRAM_IMAGE_SEARCHER:
					searcher = ImageSearcherFactory.createJpegCoefficientHistogramImageSearcher(numResults);
					break;
				case VISUAL_WORDS_IMAGE_SEARCHER:
					searcher = new VisualWordsImageSearcher(numResults, DocumentBuilder.FIELD_NAME_SURF + DocumentBuilder.FIELD_NAME_BOVW);
					break;
				case JOINT_HISTOGRAM_IMAGE_SEARCHER:
					searcher = ImageSearcherFactory.createJointHistogramImageSearcher(numResults);
					break;
				case OPPONENT_HISTOGRAM_IMAGE_SEARCHER:
					searcher = ImageSearcherFactory.createOpponentHistogramSearcher(numResults);
					break;
				case LUMINANCE_LAYOUT_IMAGE_SEARCHER:
					searcher = ImageSearcherFactory.createLuminanceLayoutImageSearcher(numResults);
					break;
				case PHOG_IMAGE_SEARCHER:
					searcher = ImageSearcherFactory.createPHOGImageSearcher(numResults);
					break;
				default:
					searcher = ImageSearcherFactory.createColorLayoutImageSearcher(numResults);
					break;
			}
			ImageSearchHits hits = searcher.search(ImageIO.read(searchedImageStream), reader);
			for (int i = 0; i < hits.length(); i++) {
				String fileIdentifier = hits.doc(i).getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue();
				similarImageFilePathList.add(new ImageSearchResult(fileIdentifier, hits.score(i)));
			}
			reader.close();
		} catch (Exception e) {
		}
		return similarImageFilePathList;
	}
}
