package test;
import java.io.IOException;

import mapreduce.Mapper;
import stream.BZIP2InputStream.Spider;

public class TestMapper<T> implements Mapper<T>{
		private Spider spider;
		private Integer count;
		public TestMapper(Spider spider) {
			super();
			this.spider = spider;
			count = 0;
		}

		@Override
		public void doMap() {
			try {
				byte[] bytTemp = this.spider.readLine();
				if(bytTemp == null) return;
				String line = new String(bytTemp);
				count = 1;
				while(line != null) {
					bytTemp = this.spider.readLine();
					if(bytTemp == null) return;
					line = new String(bytTemp);
					count++;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public T getResult() {
			return (T)count;
		}
		
		

	}
	