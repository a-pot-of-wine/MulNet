package your.org.myapp.internal.alignnetworks;

import java.io.IOException;
import java.util.ArrayList;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.NormOps_DDRM;
import org.ejml.dense.row.decomposition.eig.SwitchingEigenDecomposition_DDRM;
import org.ejml.simple.ops.SimpleOperations_DDRM;

public class SNF {
	private SimpleOperations_DDRM simpleOps=new SimpleOperations_DDRM();
	private DMatrixRMaj fusionMatrix;
	private int nSample;
	private int mMeasurements;
	private double mju=0.5;
	private double precision=1e-6;
	private int kKNN;
	
	public SNF(int nSample,ArrayList<double[][]>data) throws Exception {
		this.nSample=nSample;
		this.mMeasurements=data.size();
		this.kKNN=nSample/10;
		createSNF(this.mju,this.precision,this.kKNN,nSample,data);
	}
	public SNF(int kKNN,int nSample,ArrayList<double[][]>data) throws Exception {
		this.nSample=nSample;
		this.mMeasurements=data.size();
		this.kKNN=kKNN;
		createSNF(this.mju,this.precision,kKNN,nSample,data);
	}
	
	public SNF(double precision,int kKNN,int nSample,ArrayList<double[][]>data) throws Exception {
		this.nSample=nSample;
		this.mMeasurements=data.size();
		this.kKNN=kKNN;
		this.precision=precision;
		createSNF(this.mju,precision,kKNN,nSample,data);
	}
	
	public SNF(double mju,double precision,int kKNN,int nSample,ArrayList<double[][]>data) throws Exception {
		this.nSample=nSample;
		this.mMeasurements=data.size();
		this.mju=mju;
		this.precision=precision;
		this.kKNN=kKNN;
		createSNF(mju, precision, kKNN, nSample, data);
	}
	
	public void createSNF(double mju,double precision,int kKNN,int nSample,ArrayList<double[][]>data)throws Exception {
		if(mju>0.8||mju<0.3)
			throw new Exception("Parameter mju should in [0.3,0.8].");
		else if(precision>1e-2||precision<1e-7)
			throw new Exception("Parameter precision should in [1e-7,1e-2].");
		else if(nSample<=0)
			throw new Exception("Parameter nSample should be larger than 0");
		else if(kKNN<=0||kKNN>nSample)
			throw new Exception("Parameter kKNN should in [1,nSample].");
		else if(data==null)
			throw new Exception("Parameter data should not be null.");
		else {
			
			ArrayList<DMatrixRMaj> similarityMatrixList=new ArrayList<DMatrixRMaj>();
			ArrayList<DMatrixRMaj> localSimilarityMatrixList=new ArrayList<DMatrixRMaj>();
			
			for(int m=0;m<mMeasurements;m++) {
				if(data.get(m).length!=nSample)
					throw new Exception("The samples of all data should be equal to nSample parameter.");
			}
			similarityMatrixList=kNeighborSimilarityMatrix(distance(data));
			normalizedSimilarityMatrix(similarityMatrixList);
			localSimilarityMatrixList=localSimilarityMatrix(similarityMatrixList);
			fusionMatrix=fusion(similarityMatrixList, localSimilarityMatrixList);
		}
		
	}
	
	/*
	 * X:initial matrix from data.
	 * Y=X.^2*ones(m,n) or Y=[sumRow(X.^2),sumRow(X.^2),...] nColums.
	 * dist=Y+Y'-2X*X'
	 */
	public ArrayList<DMatrixRMaj> distance(ArrayList<double[][]> data) {
		
		ArrayList<DMatrixRMaj> distMatrixList=new ArrayList<DMatrixRMaj>();
		for(int m=0;m<mMeasurements;m++) {
			//calculate Y
			DMatrixRMaj X=new DMatrixRMaj(data.get(m));
			int Xcols=X.getNumCols();
			DMatrixRMaj X2=new DMatrixRMaj(nSample,Xcols);
			simpleOps.elementMult(X, X, X2);
			DMatrixRMaj sumRow=new DMatrixRMaj(nSample,1);
			CommonOps_DDRM.sumRows(X2, sumRow);
			DMatrixRMaj Y=new DMatrixRMaj(nSample,nSample);
			for(int j=0;j<nSample;j++)
				simpleOps.setColumn(Y, j, 0, sumRow.getData());
			
			DMatrixRMaj dist=new DMatrixRMaj(nSample,nSample);
			DMatrixRMaj yTrans=new DMatrixRMaj(nSample,nSample);
			DMatrixRMaj xTrans=new DMatrixRMaj(Xcols,nSample);
			simpleOps.transpose(Y,yTrans);
			simpleOps.transpose(X, xTrans);
			simpleOps.plus(Y, yTrans, Y);
			simpleOps.mult(X, xTrans, dist);
			simpleOps.divide(dist, 1.0/2.0, dist);
			simpleOps.minus(Y, dist, dist);
			distMatrixList.add(dist);
		}
		return distMatrixList;
		
	}
	
	
	/*
	 * W matrix in SNF:
	 * mean=(1/k)[sum(kneighbors),sum(kneighbors),...,sum(kneighbors)]
	 * epsilon=(1/3)(mean+mean'+sqrt(dist))
	 * simi=exp(-dist./(mju.*epsilon))
	 */
	public ArrayList<DMatrixRMaj> kNeighborSimilarityMatrix(ArrayList<DMatrixRMaj> distMatrixList){
		
		ArrayList<DMatrixRMaj> similarityMatrixList=new ArrayList<DMatrixRMaj>();
		for(int m=0;m<mMeasurements;m++) {
			DMatrixRMaj dist=distMatrixList.get(m);
			DMatrixRMaj distSqrt =new DMatrixRMaj(nSample,nSample);
			simpleOps.elementPower(dist,0.5,distSqrt);
			DMatrixRMaj mean=new DMatrixRMaj(nSample,nSample);
			// calculate mean
			double[] sum=new double[nSample];
			for(int i=0;i<nSample;i++) {
				double[] rowData=new double[nSample];
				Integer[] rowIndex=new Integer[nSample];
				DMatrixRMaj row=new DMatrixRMaj(1,nSample);
				CommonOps_DDRM.extractRow(dist, i, row);
				rowData=row.getData();
				rowIndex=UtilEjml.sortByIndex(rowData, nSample); // small to large
				sum[i]=0.0;
				for(int k=1;k<kKNN+1;k++) //dist(i,i)=0; so,rowIndex[0]=i,rowData[i]=0
					sum[i]+=rowData[rowIndex[k]];
			}
			for(int j=0;j<nSample;j++)
				simpleOps.setColumn(mean, j, 0, sum);
			simpleOps.divide(mean, kKNN, mean);
			//calculate epsilon
			DMatrixRMaj meanTrans=new DMatrixRMaj(nSample,nSample);
			simpleOps.transpose(mean, meanTrans);
			simpleOps.plus(mean, meanTrans, mean);
			simpleOps.plus(mean, distSqrt, mean);
			DMatrixRMaj epsilon=new DMatrixRMaj(nSample,nSample);
			simpleOps.divide(mean, 3.0, epsilon);
			double eps=2.2204E-16;
			simpleOps.plus(epsilon, eps, epsilon);// Prevents the divisor from being 0,so plus a very small number:eps
			
			//similarityMatrix
			DMatrixRMaj similarityMatrix=new DMatrixRMaj(nSample,nSample);
			simpleOps.divide(epsilon, 1.0/mju, epsilon);  //mju*epsilon
			simpleOps.elementDiv(dist, epsilon, similarityMatrix);
			simpleOps.divide(similarityMatrix, -1.0, similarityMatrix);
			simpleOps.elementExp(similarityMatrix, similarityMatrix);
			
			DMatrixRMaj similarityMatrixTrans=new DMatrixRMaj(nSample,nSample);
			simpleOps.transpose(similarityMatrix, similarityMatrixTrans);
			simpleOps.plus(similarityMatrix, similarityMatrixTrans, similarityMatrix);
			simpleOps.divide(similarityMatrix, 2.0, similarityMatrix);
			similarityMatrixList.add(similarityMatrix);
		}
		
		return similarityMatrixList;
		
	}
	
	/*//P matrix in SNF:
	 * copy=simi.copy(),then set copy's all dialog elements to 0 
	 * sumRowRepeat=[sumRow(copy),sumRow(copy),...,sumRow(copy)] ,nSample columns
	 * P=( simi./sumRowRepeat)./2 then set P's all dialog elements to 0.5
	 * then P=(P+P')/2
	 */
	public void normalizedSimilarityMatrix(ArrayList<DMatrixRMaj> similarityMatrixList) {
		
		for(int m=0;m<mMeasurements;m++) {
			DMatrixRMaj simi=similarityMatrixList.get(m);
			DMatrixRMaj copy=simi.copy();
			for(int i=0;i<nSample;i++)
				copy.set(i,i,0);
			DMatrixRMaj sumRow=new DMatrixRMaj(nSample,1);
			CommonOps_DDRM.sumRows(copy, sumRow);
			DMatrixRMaj sumRowRepeat=new DMatrixRMaj(nSample,nSample);
			for(int j=0;j<nSample;j++) {
				simpleOps.setColumn(sumRowRepeat, j, 0, sumRow.getData());
			}
			simpleOps.elementDiv(simi, sumRowRepeat, simi);
			simpleOps.divide(simi, 2.0, simi);
			
			for(int i=0;i<nSample;i++)
				simi.set(i, i, 0.5);
			
			DMatrixRMaj simiTrans=new DMatrixRMaj(nSample,nSample);
			simpleOps.transpose(simi, simiTrans);
			simpleOps.plus(simi, simiTrans, simi);
			simpleOps.divide(simi, 2.0, simi);
			
		}
		
	}
	
	
	/*
	 * S matrix in SNF:
	 *
	 */
	public ArrayList<DMatrixRMaj> localSimilarityMatrix(ArrayList<DMatrixRMaj> similarityMatrixList){
		
		ArrayList<DMatrixRMaj> localSimilarityMatrixList=new ArrayList<DMatrixRMaj>();
		for(int m=0;m<mMeasurements;m++) {
			DMatrixRMaj simi=similarityMatrixList.get(m).copy();
			
			for(int i=0;i<nSample;i++) {
				double[] rowData=new double[nSample];
				Integer[] rowIndex=new Integer[nSample];
				DMatrixRMaj row=new DMatrixRMaj(1,nSample);
				CommonOps_DDRM.extractRow(simi, i, row);
				rowData=row.getData();
				rowIndex=UtilEjml.sortByIndex(rowData, nSample); // small to large
				for(int k=0;k<nSample-kKNN;k++) // the elements which are not belong to i sample will be set to 0
					simi.set(i, rowIndex[k], 0);
			}
			
			DMatrixRMaj sumRow=new DMatrixRMaj(nSample,1);
			CommonOps_DDRM.sumRows(simi, sumRow);
			DMatrixRMaj sumRowRepeat=new DMatrixRMaj(nSample,nSample);
			for(int j=0;j<nSample;j++) {
				simpleOps.setColumn(sumRowRepeat, j, 0, sumRow.getData());
			}
			
			DMatrixRMaj localSimilarityMatrix=new DMatrixRMaj(nSample,nSample);
			simpleOps.elementDiv(simi, sumRowRepeat, localSimilarityMatrix);
			localSimilarityMatrixList.add(localSimilarityMatrix);
				
		}
		return localSimilarityMatrixList;
	}
	
	/*
	 * Pv=Sv*{sum(Pk|k!=v)/(m-1)}*transpose(Sv)
	 * steps coming to 50 or reaching precision will end iteration.
	 * precision defined by normP2: ||Pv+1 - Pv||/||Pv||
	 */
	public DMatrixRMaj fusion(ArrayList<DMatrixRMaj> similarityMatrixList,ArrayList<DMatrixRMaj> localSimilarityMatrixList) throws IOException {
		
		DMatrixRMaj PvAverage=new DMatrixRMaj(nSample,nSample);
		DMatrixRMaj Pv1Average=new DMatrixRMaj(nSample,nSample);
		int t=0;
		for(t=1;t<=30;t++) {
			ArrayList<DMatrixRMaj> newPList =new ArrayList<DMatrixRMaj>();
			for(int v=0;v<mMeasurements;v++) {
				DMatrixRMaj P=similarityMatrixList.get(v).copy();
				DMatrixRMaj S=localSimilarityMatrixList.get(v);
				DMatrixRMaj sTrans=new DMatrixRMaj(nSample,nSample);
				simpleOps.transpose(S, sTrans);
				DMatrixRMaj pSum=new DMatrixRMaj(nSample,nSample);
				simpleOps.fill(pSum, 0.0);
				for(int k=0;k<mMeasurements;k++) {
					if(k!=v) {
						DMatrixRMaj Pk=similarityMatrixList.get(k);
						simpleOps.plus(Pk, pSum, pSum);
					}
				}
				DMatrixRMaj temp=new DMatrixRMaj(nSample,nSample);
				simpleOps.mult(S, pSum, temp);
				simpleOps.mult(temp, sTrans, P);
				simpleOps.divide(P, mMeasurements-1, P);
				//normalize
				DMatrixRMaj eye=CommonOps_DDRM.identity(nSample);
				simpleOps.plus(P, eye, P);
				DMatrixRMaj PTrans=new DMatrixRMaj(nSample,nSample);
				simpleOps.transpose(P, PTrans);
				simpleOps.plus(P, PTrans, P);
				simpleOps.divide(P, 2.0, P);
				newPList.add(P);
				
			}
			similarityMatrixList=newPList;
			// average P
			if(t%5==4) {
				simpleOps.fill(PvAverage, 0.0);
				for(int m=0;m<mMeasurements;m++) {
					simpleOps.plus(similarityMatrixList.get(m), PvAverage, PvAverage);
				}
				simpleOps.divide(PvAverage, mMeasurements, PvAverage);	
			}
			//average Pv+1
			if(t%5==0) {
				
				simpleOps.fill(Pv1Average, 0.0);
				for(int m=0;m<mMeasurements;m++) {
					simpleOps.plus(similarityMatrixList.get(m), Pv1Average, Pv1Average);
				}
				simpleOps.divide(Pv1Average, mMeasurements, Pv1Average);
				DMatrixRMaj minusPvPv1=new DMatrixRMaj(nSample,nSample);
				simpleOps.minus(Pv1Average, PvAverage, minusPvPv1);
				double precisionNow=NormOps_DDRM.normF(minusPvPv1)/NormOps_DDRM.normF(PvAverage);
				if(precisionNow<=precision) {
					break;
				}
				
			}
		}
		// normalize fusion result 
		DMatrixRMaj sumRow=new DMatrixRMaj(nSample,1);
		CommonOps_DDRM.sumRows(Pv1Average, sumRow);
		DMatrixRMaj sumRowRepeat=new DMatrixRMaj(nSample,nSample);
		for(int j=0;j<nSample;j++)
			simpleOps.setColumn(sumRowRepeat, j, 0, sumRow.getData());
		simpleOps.elementDiv(Pv1Average.copy(), sumRowRepeat, Pv1Average);
		DMatrixRMaj Pv1Trans=new DMatrixRMaj(nSample,nSample);
		simpleOps.transpose(Pv1Average, Pv1Trans);
		simpleOps.plus(Pv1Average, Pv1Trans, Pv1Average);
		DMatrixRMaj eye=CommonOps_DDRM.identity(nSample);
		simpleOps.plus(Pv1Average, eye, Pv1Average);
		simpleOps.divide(Pv1Average, 2.0, Pv1Average);
		return Pv1Average;
	}
	
	public DMatrixRMaj getFusionMatrix() {
		DMatrixRMaj Matrix=fusionMatrix.copy();
		return Matrix;
	}
	
	public double[][] getFusionMatrixArray(){
		
		return null;
	}
	
	/*
	 * L=D-W,Ln=D^(-1/2)*L*D^(-1/2)
	 * find k smallest eigenvalues and thier eigenvectors x1,x2,...,xk£¬F[x1,x2,...,xk]
	 * then use K-means method to cluster F
	 */
	public int[] spectralCluster(int kCluster) {
		//caluculate L
		DMatrixRMaj L=new DMatrixRMaj(nSample,nSample);
		DMatrixRMaj W=fusionMatrix.copy();
		DMatrixRMaj sumRow=new DMatrixRMaj(nSample,1);
		CommonOps_DDRM.sumRows(W, sumRow);
		DMatrixRMaj D=CommonOps_DDRM.diag(sumRow.data);
		simpleOps.minus(D, W, L);
		
		//calculate D^(-1/2)
		double eps=2.2204E-16;
		for(int i=0;i<nSample;i++) //avoid dividing by zero
			if(D.get(i, i)==0)
				D.set(i, i, eps);
		//sumRow.set(0, 0, 4);
		simpleOps.elementPower(sumRow, -0.5, sumRow);
		D=CommonOps_DDRM.diag(sumRow.data);
		
		//calculate Ln
		DMatrixRMaj Ln=new DMatrixRMaj(nSample,nSample);
		DMatrixRMaj temp=new DMatrixRMaj(nSample,nSample);
		simpleOps.mult(D, L, temp);
		simpleOps.mult(temp, D, Ln);
		SwitchingEigenDecomposition_DDRM EigenOps=new SwitchingEigenDecomposition_DDRM(nSample);
		EigenOps.decompose(Ln);
		
		//get F
		int eigNum=EigenOps.getNumberOfEigenvalues();
		double[] eigenValues=new double[eigNum]; //according to Ln'symmetry, eigenValue must be real number
		DMatrixRMaj[] eigenVectors=new DMatrixRMaj[eigNum];
		for(int i=0;i<eigNum;i++) {
			eigenValues[i]=EigenOps.getEigenvalue(i).getReal();
			eigenVectors[i]=EigenOps.getEigenVector(i).copy();
		}
		Integer[] eigenIndex=new Integer[eigNum];
		eigenIndex=UtilEjml.sortByIndex(eigenValues,eigNum);
		DMatrixRMaj Ftemp=new DMatrixRMaj(nSample,kCluster);
		for(int k=0;k<kCluster;k++) {
			simpleOps.setColumn(Ftemp, k, 0, eigenVectors[eigenIndex[k]].getData());
		}
		DMatrixRMaj[] F=new DMatrixRMaj[nSample];
		for(int i=0;i<nSample;i++) {
			DMatrixRMaj f=new DMatrixRMaj(1,kCluster);
			CommonOps_DDRM.extractRow(Ftemp, i, f);
			F[i]=f;
			
		}
		
		//K-means
		DMatrixRMaj[] center=new DMatrixRMaj[kCluster];
		int[] group=new int[nSample];
		ArrayList<Integer> randomList=new ArrayList<Integer>();
		
		while(randomList.size()<kCluster) {
			int now=(int)(Math.random()*nSample);
			if(randomList.contains(now))
				continue;
			else
				randomList.add(now);	
		}
		for(int i=0;i<kCluster;i++)
			center[i]=F[randomList.get(i)].copy();
		
		boolean clusterVaryFlag=true;
		while(clusterVaryFlag) {
			clusterVaryFlag=false;
			// update group
			for(int i=0;i<nSample;i++) {
				DMatrixRMaj diffMatrix=new DMatrixRMaj(1,kCluster);
				simpleOps.minus(F[i].copy(), center[group[i]].copy(), diffMatrix);
				double minDiff=NormOps_DDRM.normF(diffMatrix);
				for(int k=0;k<kCluster;k++) {
					simpleOps.minus(F[i], center[k], diffMatrix);
					double nowKDiff=NormOps_DDRM.normF(diffMatrix);
					if(nowKDiff<minDiff) {
						clusterVaryFlag=true;
						group[i]=k;
						minDiff=nowKDiff;
					}
				}
			}
			
			//update center
			if(clusterVaryFlag) {
				for(int k=0;k<kCluster;k++) {
					DMatrixRMaj centerK=new DMatrixRMaj(1,kCluster);
					simpleOps.fill(centerK, 0.0);
					int countK=0;
					for(int i=0;i<nSample;i++) {
						if(group[i]==k) {
							countK++;
							simpleOps.plus(centerK, F[i], centerK);
						}
					}
					simpleOps.divide(centerK, countK, center[k]);
				}
			}
		}
		
		return group;
		
	}
	
}
