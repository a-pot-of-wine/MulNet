package your.org.myapp.internal.alignnetworks;

import java.util.ArrayList;
import java.util.HashSet;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.simple.ops.SimpleOperations_DDRM;

public class ConMod {
	private ArrayList<DMatrixRMaj> multiNetworks;
	private int nNode;
	private int kHiddenFactors;
	private double[] lambdaArray;
	private double xita;
	private int maxIter;
	private int numNetWorks;
	private ArrayList<HashSet<Integer>> conModResult;
	private SimpleOperations_DDRM simpleOps=new SimpleOperations_DDRM();
	
	public ConMod() {
		
	}
	public ConMod(ArrayList<DMatrixRMaj> multiNetworks,boolean isAdjacentMatix,double[] lambdaArray,int nNode,int kHiddenFactors,double xita,int maxIter) throws ConModException,Exception{
		
		//  the input checking is ignored.
		 
		this.multiNetworks=multiNetworks;
		this.nNode=nNode;
		this.kHiddenFactors=kHiddenFactors;
		this.lambdaArray=lambdaArray;
		this.xita=xita;
		this.maxIter=maxIter;
		numNetWorks=multiNetworks.size();
		
		DMatrixRMaj strengthMatrix=new DMatrixRMaj(nNode,nNode);
		DMatrixRMaj participationMatrix=new DMatrixRMaj(nNode,nNode);
		
		featureNets(multiNetworks,isAdjacentMatix,strengthMatrix,participationMatrix);
		
		DMatrixRMaj HcMatrix=multiViewNMF(strengthMatrix,participationMatrix);
		
		this.conModResult=moduleNodesSelection(HcMatrix);
	}
	
	public ArrayList<HashSet<Integer>> getConModResult(){
		ArrayList<HashSet<Integer>> result=new ArrayList<HashSet<Integer>>();
		for(int i=0;i<conModResult.size();i++) {
			result.add((HashSet<Integer>) conModResult.get(i).clone());
		}
		return result;
	}
	
	
	public void featureNets(ArrayList<DMatrixRMaj> multiNetworks,boolean isAdjacentMatix,DMatrixRMaj strengthMatrixOut,DMatrixRMaj participationMatrixOut) {
		
		DMatrixRMaj A=new DMatrixRMaj(nNode,nNode);
		DMatrixRMaj temp=new DMatrixRMaj(nNode,nNode);
		if(isAdjacentMatix==true) {
			double weightMax=1.0;
			double weightMin=0.0;
			for(int iNet=0;iNet<multiNetworks.size();iNet++) {
				DMatrixRMaj theMatrix=multiNetworks.get(iNet);
				weightMax=simpleOps.elementMaxAbs(theMatrix);
				
				DMatrixRMaj matrixWeight1=new DMatrixRMaj(nNode,nNode);
				simpleOps.divide(theMatrix, weightMax, matrixWeight1);
				
				DMatrixRMaj matrixWeight2=new DMatrixRMaj(nNode,nNode);
				double log9999=Math.log(9999);
				double log9999_2=log9999*2;
				simpleOps.divide(matrixWeight1, -1.0/log9999_2, matrixWeight2);
				simpleOps.plus(matrixWeight2, log9999, matrixWeight2);
				simpleOps.elementExp(matrixWeight2, matrixWeight2);
				simpleOps.plus(matrixWeight2, 1, matrixWeight2);
				simpleOps.elementPower(matrixWeight2, -1, matrixWeight2);
				
				for(int i=0;i<nNode;i++)
					for(int j=0;j<nNode;j++) 
						if(matrixWeight1.get(i, j)<=0.3)  
							matrixWeight2.set(i, j, 0.0);
				
				simpleOps.plus(A, matrixWeight2, A);
				simpleOps.elementPower(matrixWeight2, 2, matrixWeight2);
				simpleOps.plus(temp, matrixWeight2, temp);
				simpleOps.plus(strengthMatrixOut, theMatrix, strengthMatrixOut);
				
			}
		}
		else{
			
			double weightMax=1.0;
			double weightMin=0.0;
			for(int iNet=0;iNet<multiNetworks.size();iNet++) {
				DMatrixRMaj theMatrix=multiNetworks.get(iNet);
				int edgeCount=theMatrix.numRows;
				int colCount=theMatrix.numCols;
				if (colCount==3) { //weighted network
					DMatrixRMaj weighCol=new DMatrixRMaj(edgeCount,1);
					CommonOps_DDRM.extractColumn(theMatrix, 2, weighCol);
					weightMax=simpleOps.elementMaxAbs(weighCol);
				}
				double weight;
				for(int e=0;e<edgeCount;e++) {
					int iRow=(int)theMatrix.get(e, 0);
					int jCol=(int)theMatrix.get(e, 1);
					if (colCount==3)//weighted network
						weight=Math.abs(theMatrix.get(e, 2));
					else //unweighted network
						weight=1.0;
					double valueTemp=strengthMatrixOut.get(iRow, jCol)+weight;
					strengthMatrixOut.set(iRow, jCol, valueTemp);
					strengthMatrixOut.set(jCol, iRow, valueTemp);
					
					double weight_1=(weight-weightMin)/(weightMax-weightMin);
					double weight_2=1/(1+Math.exp(Math.log(9999)-2*Math.log(9999)*weight_1));
					if(weight_1<0.3)
						weight_2=0;
					
					valueTemp=A.get(iRow, jCol)+weight_2;
					A.set(iRow, jCol, valueTemp);
					A.set(jCol, iRow, valueTemp);
					
					valueTemp=temp.get(iRow,jCol)+weight_2*weight_2;
					temp.set(iRow, jCol, valueTemp);
					temp.set(jCol, iRow, valueTemp);
				}
			}
		}
		
		DMatrixRMaj A2=new DMatrixRMaj(nNode,nNode);
		simpleOps.elementPower(A, 2, A2);
		simpleOps.elementDiv(temp, A2, temp);
		simpleOps.divide(temp, -1, temp);
		simpleOps.plus(temp, 1, temp);
		simpleOps.divide(temp, (numNetWorks-1.0)/numNetWorks, participationMatrixOut);
		
		simpleOps.divide(A, numNetWorks, strengthMatrixOut);
		
		for(int i=0;i<nNode;i++)
			for(int j=0;j<nNode;j++) {
				if(Double.isInfinite(participationMatrixOut.get(i, j))||Double.isNaN(participationMatrixOut.get(i, j)))
					participationMatrixOut.set(i, j, 0);
				if(i==j) {
					participationMatrixOut.set(i, j, 0);
					strengthMatrixOut.set(i, j, 0);
				}
			}
		
		
	}
	
	
	// return Hc(the consensus factor matrix)
	public DMatrixRMaj multiViewNMF(DMatrixRMaj strengthMatrix,DMatrixRMaj participationMatrix) {
		
		
		ArrayList<DMatrixRMaj> X=new ArrayList<DMatrixRMaj>();
		X.add(strengthMatrix);
		X.add(participationMatrix);
		
		ArrayList<DMatrixRMaj> H=new ArrayList<DMatrixRMaj>();
		H.add(new DMatrixRMaj(nNode,kHiddenFactors));
		H.add(new DMatrixRMaj(nNode,kHiddenFactors));
		DMatrixRMaj Hc=new DMatrixRMaj(nNode,kHiddenFactors);
		
		DMatrixRMaj tempXSize1=new DMatrixRMaj(nNode,nNode);
		DMatrixRMaj tempXSize2=new DMatrixRMaj(nNode,nNode);
		for(int i=0;i<2;i++) {	
			double traceSqr;
			simpleOps.transpose(X.get(i), tempXSize1);
			simpleOps.mult(tempXSize1, X.get(i), tempXSize2);
			traceSqr=Math.sqrt(simpleOps.trace(tempXSize2));
			simpleOps.divide(X.get(i), traceSqr, X.get(i));
		}
		
		int iterNum=30;
		if(nNode>2000)
			iterNum=20;
		
		
		DMatrixRMaj H_init=new DMatrixRMaj(nNode,kHiddenFactors);
		for(int i=0;i<nNode;i++)
			for(int j=0;j<kHiddenFactors;j++) 
				H_init.set(i, j, Math.random());
		
		H.set(0, SNMF(X.get(0), H_init,iterNum, 1e-5));
		H.set(1, SNMF(X.get(1), H.get(0),iterNum, 1e-5));
		
		DMatrixRMaj tempHSize=new DMatrixRMaj(nNode,kHiddenFactors);
		for(int i=0;i<2;i++) {
			simpleOps.divide(H.get(i), 1.0/lambdaArray[i],tempHSize);
			simpleOps.plus(Hc, tempHSize, Hc);
		}
		simpleOps.divide(Hc, lambdaArray[0]+lambdaArray[1], Hc);
		
		double obj_old = 0,obj_body,obj_consensus;
		DMatrixRMaj HTranspose=new DMatrixRMaj(kHiddenFactors,nNode);
		for(int i=0;i<2;i++) {
			simpleOps.transpose(H.get(i), HTranspose);
			simpleOps.mult(H.get(i), HTranspose, tempXSize1);
			simpleOps.minus(X.get(i), tempXSize1, tempXSize1);
			obj_body=Math.pow(simpleOps.normF(tempXSize1), 2);
			
			simpleOps.minus(H.get(i), Hc, tempHSize);
			obj_consensus=Math.pow(simpleOps.normF(tempHSize), 2);
			
			obj_old=obj_old+obj_body+lambdaArray[i]*obj_consensus;
		}
		double objValue=obj_old;
		
		
		
		for(int iter=0;iter<maxIter;iter++) {
			int maxIterforView = 40;
			for(int i=0;i<2;i++) 
				H.set(i, SNMFforView(X.get(i), Hc, H.get(i),lambdaArray[i], maxIterforView,1e-6));
			
			Hc.zero();
			for(int i=0;i<2;i++) {
				simpleOps.divide(H.get(i), 1.0/lambdaArray[i],tempHSize);
				simpleOps.plus(Hc, tempHSize, Hc);
			}
			simpleOps.divide(Hc, lambdaArray[0]+lambdaArray[1], Hc);
			
			double obj=0;
			for(int i=0;i<2;i++) {
				simpleOps.transpose(H.get(i), HTranspose);
				simpleOps.mult(H.get(i), HTranspose, tempXSize1);
				simpleOps.minus(X.get(i), tempXSize1, tempXSize1);
				obj_body=Math.pow(simpleOps.normF(tempXSize1), 2);
				
				simpleOps.minus(H.get(i), Hc, tempHSize);
				obj_consensus=Math.pow(simpleOps.normF(tempHSize), 2);
				
				obj=obj+obj_body+lambdaArray[i]*obj_consensus;
			}
			
			double delta=obj_old-obj;
			obj_old=obj;
			if(delta<1e-6)
				break;
		}
		return Hc;
	}
	
	// return H(factor matrix of XMatrix)
	public DMatrixRMaj SNMF(DMatrixRMaj XMatrix, DMatrixRMaj H_init, int iterNum,double epsilon) {
		
		DMatrixRMaj tempHSize=new DMatrixRMaj(nNode,kHiddenFactors);
		DMatrixRMaj tempXSize=new DMatrixRMaj(nNode,nNode);
		DMatrixRMaj tempKSize=new DMatrixRMaj(kHiddenFactors,kHiddenFactors);
		DMatrixRMaj HTranspose=new DMatrixRMaj(kHiddenFactors,nNode);
		DMatrixRMaj XTranspose=new DMatrixRMaj(nNode,nNode);
		
		DMatrixRMaj X=new DMatrixRMaj(XMatrix);
		simpleOps.transpose(X, XTranspose);
		simpleOps.mult(XTranspose, X, tempXSize);
		double traceSqr=Math.sqrt(simpleOps.trace(tempXSize));
		simpleOps.divide(X, traceSqr, X);
		
		DMatrixRMaj H=new DMatrixRMaj(H_init);
		simpleOps.transpose(H,HTranspose);
		simpleOps.mult(HTranspose, H, tempKSize);
		traceSqr=Math.sqrt(simpleOps.trace(tempKSize));
		simpleOps.divide(H, traceSqr, H);
		
		double obj_old,beta=0.5;
		simpleOps.transpose(H,HTranspose);
		simpleOps.mult(H, HTranspose, tempXSize);
		simpleOps.minus(X, tempXSize, tempXSize);
		obj_old=Math.pow(simpleOps.normF(tempXSize), 2);
		
		DMatrixRMaj temp_1=new DMatrixRMaj(nNode,kHiddenFactors);
		DMatrixRMaj temp_2=new DMatrixRMaj(nNode,kHiddenFactors);
		for(int iter=0;iter<iterNum;iter++) {
			simpleOps.mult(X, H, temp_1);
			simpleOps.transpose(H,HTranspose);
			simpleOps.mult(HTranspose, H, tempKSize);
			simpleOps.mult(H, tempKSize, temp_2);
			
			double eps=2.2204e-16;
			simpleOps.plus(temp_2, eps, temp_2);
			simpleOps.elementDiv(temp_1, temp_2, tempHSize);
			simpleOps.divide(tempHSize, 1.0/beta, tempHSize);
			simpleOps.plus(tempHSize, 1-beta, tempHSize);
			simpleOps.elementMult(H, tempHSize, H);
			
			double obj;
			simpleOps.transpose(H,HTranspose);
			simpleOps.mult(H, HTranspose, tempXSize);
			simpleOps.minus(X, tempXSize, tempXSize);
			obj=Math.pow(simpleOps.normF(tempXSize), 2);
			
			double delta=obj_old-obj;
			obj_old=obj;
			if(delta<epsilon)
				break;
		}
		return H;
	}
	
	// return H:Multi-View Non-negative symmetric Matrix factorization for each view,
	public DMatrixRMaj SNMFforView(DMatrixRMaj XMatrix, DMatrixRMaj HcMatrix, DMatrixRMaj HMatrix,double lambda, int maxIterView, double epsilon) {
		
		double obj_old;
		DMatrixRMaj HTranspose=new DMatrixRMaj(kHiddenFactors,nNode);
		DMatrixRMaj tempXSize=new DMatrixRMaj(nNode,nNode);
		DMatrixRMaj tempHSize=new DMatrixRMaj(nNode,kHiddenFactors);
		
		DMatrixRMaj X=new DMatrixRMaj(XMatrix);
		DMatrixRMaj H=new DMatrixRMaj(HMatrix);
		DMatrixRMaj Hc=new DMatrixRMaj(HcMatrix);
		
		simpleOps.transpose(H, HTranspose);
		simpleOps.mult(H, HTranspose, tempXSize);
		simpleOps.minus(X, tempXSize, tempXSize);
		simpleOps.minus(H, Hc, tempHSize);
		obj_old=Math.pow(simpleOps.normF(tempXSize), 2)+lambda*Math.pow(simpleOps.normF(tempHSize), 2);
		
		DMatrixRMaj temp_1=new DMatrixRMaj(nNode,kHiddenFactors);
		DMatrixRMaj temp_2=new DMatrixRMaj(nNode,kHiddenFactors);
		double obj_body=0,obj_consensus=0,obj=0;
		for(int iter=0;iter<maxIterView;iter++) {
			simpleOps.mult(X, H, tempHSize);
			simpleOps.divide(tempHSize, 1.0/2.0, temp_1);
			simpleOps.divide(Hc, 1.0/lambda, tempHSize);
			simpleOps.plus(temp_1, tempHSize, temp_1);
			
			simpleOps.transpose(H,HTranspose);
			simpleOps.mult(HTranspose, H, tempHSize);
			simpleOps.mult(H, tempHSize, temp_2);
			simpleOps.divide(temp_2, 1.0/2.0, temp_2);
			simpleOps.divide(H, 1.0/lambda, tempHSize);
			simpleOps.plus(temp_2, tempHSize, temp_2);
			
			double eps=2.2204e-16;
			simpleOps.plus(temp_2, eps, temp_2);
			simpleOps.elementDiv(temp_1, temp_2, temp_1);
			simpleOps.elementMult(H, temp_1, H);
			
			simpleOps.transpose(H, HTranspose);
			simpleOps.mult(H, HTranspose, tempXSize);
			simpleOps.minus(X, tempXSize, tempXSize);
			obj_body=Math.pow(simpleOps.normF(tempXSize), 2);
			
			simpleOps.minus(H, Hc, tempHSize);
			obj_consensus=Math.pow(simpleOps.normF(tempHSize), 2);
			obj=obj_body+lambda*obj_consensus;
			
			double delta=obj_old-obj;
			obj_old=obj;
			if(delta<epsilon)
				break;
		}
		
		return H;
	}
	
	public ArrayList<HashSet<Integer>> moduleNodesSelection(DMatrixRMaj HcMatrix){
		
		DMatrixRMaj Hc=new DMatrixRMaj(HcMatrix);
		ArrayList< HashSet<Integer> > candidateModules=new ArrayList< HashSet<Integer> >();
		double[] moduleSignal=new double[kHiddenFactors];
		DMatrixRMaj tempCols=new DMatrixRMaj(1,kHiddenFactors);
		CommonOps_DDRM.sumCols(Hc,tempCols);
		simpleOps.divide(tempCols, nNode, tempCols);
		double[] H_mean=tempCols.data.clone();
		
		double[] H_std=new double[kHiddenFactors];
		for(int j=0;j<kHiddenFactors;j++) {
			double sumColPower=0;
			for(int i=0;i<nNode;i++) {
				sumColPower+=(  Math.pow(Hc.get(i, j)-H_mean[j], 2) );
			}
			H_std[j]=Math.sqrt(sumColPower/(nNode));
		}
		
		for(int k=0;k<kHiddenFactors;k++) {
			double temp=H_mean[k]+xita*H_std[k];
			HashSet<Integer> tempCandidate=new HashSet<Integer>();
			double tempSignal=0;
			int count=0;
			for(int i=0;i<nNode;i++) 
				if(Hc.get(i, k)>temp) {
					 count++;
					tempCandidate.add(i+1);
					tempSignal+=Hc.get(i, k);
					
				}
			if(tempCandidate.size()==0)
				tempCandidate=null;
			candidateModules.add(tempCandidate);
			moduleSignal[k]=tempSignal/count;
		}
		DMatrixRMaj HPI=setSimilarity(candidateModules);
		ArrayList< HashSet<Integer> > modulesFinal=candidateModules;
		
		for(int i=0;i<HPI.numRows-1;i++) {
			for(int j=i+1;j<HPI.numCols;j++) {
				if(HPI.get(i, j)>0.5)
					if(moduleSignal[i]>moduleSignal[j]) {
						modulesFinal.get(i).addAll(modulesFinal.get(j));
						modulesFinal.set(j, null);
						moduleSignal[j]=0;
						for(int q=0;q<HPI.numCols;q++)
							HPI.set(j, q, 0);
						for(int q=0;q<HPI.numRows;q++)
							HPI.set(q, j, 0);
					}
					else {
						modulesFinal.get(j).addAll(modulesFinal.get(i));
						modulesFinal.set(i, null);
						moduleSignal[i]=0;
						for(int q=0;q<HPI.numCols;q++)
							HPI.set(j, q, 0);
						for(int q=0;q<HPI.numRows;q++)
							HPI.set(q, j, 0);
					}
			}
		}
		
		
		for(int i=0;i<modulesFinal.size();i++) {
			if(modulesFinal.get(i)==null||modulesFinal.get(i).size()<3) {
				modulesFinal.remove(i);
				i--;
			}
		}
		
		
		
		return modulesFinal;
	}
	
	// return HPI
	public DMatrixRMaj setSimilarity(ArrayList<HashSet<Integer>> modules) {
		
		int modulesCount=modules.size();
		DMatrixRMaj HPI=new DMatrixRMaj(modulesCount,modulesCount);
		for(int i=0;i<modulesCount-1;i++) {
			HashSet<Integer> module_i;
			if(modules.get(i)!=null)
				module_i=(HashSet<Integer>) modules.get(i).clone();
			else
				module_i=null;
			for(int j=i+1;j<modulesCount;j++) {
				HashSet<Integer> module_j;
				if(modules.get(j)!=null)
					module_j=(HashSet<Integer>) modules.get(j).clone();
				else
					module_j=null;
				if(module_i!=null&&module_j!=null) {
					double length1=Math.min(module_i.size(), module_j.size());
					HashSet<Integer> temp_i=(HashSet<Integer>) module_i.clone();
					temp_i.retainAll(module_j);
					double length2=temp_i.size();
					HPI.set(i, j, length2/length1);
				}	
			}
		}
		return HPI;
	}

}
