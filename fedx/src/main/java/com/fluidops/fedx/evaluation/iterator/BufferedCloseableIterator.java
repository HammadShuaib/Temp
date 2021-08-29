package com.fluidops.fedx.evaluation.iterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.common.iteration.LookAheadIteration;

public class BufferedCloseableIterator<T, E extends RuntimeException> extends LookAheadIteration<T, E> {
//	static Logger logger = LoggerFactory.getLogger(BufferedCloseableIterator.class);

	RuntimeException ex_ = null;
	List<T> buffer_ = new ArrayList<T>();
	Iterator<T> pos_;

	public BufferedCloseableIterator(CloseableIteration<T, E> it) {
		try {
			System.out.println("This is bufferedClosableIterator:" + it);
			while (it.hasNext()) {
				buffer_.add(it.next());
				// logger.info("row: " + buffer_.get(buffer_.size() - 1));
			}
		} catch (RuntimeException e) {
			ex_ = e;
		} finally {
			try {
				it.close();
			} catch (Exception ignore) {
			}
		}
		pos_ = buffer_.iterator();
	}

	@Override
	protected T getNextElement() {
		if (!pos_.hasNext()) {
			if (ex_ != null) {
				RuntimeException e = ex_;
				ex_ = null;
				throw e;
			}
			return null;
		}
		return pos_.next();
	}
}
