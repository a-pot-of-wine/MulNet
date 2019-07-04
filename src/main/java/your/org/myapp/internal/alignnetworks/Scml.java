package your.org.myapp.internal.alignnetworks;
/*
 * 自定义的类Scml
 * 满足接口规范，实现接口，重写抽象方法
 */
/**
 * class介绍
 * 
 */
/*SC-ML算法的实现：
	sc-ml(A, K, lambda)
	输入：
		A:N*N*M的多层邻接矩阵，N行N列M层
		K：聚类个数；
		lambda：正则化参数；
	输出：
		每个点的聚类标签
*/



import Jama.Matrix;
import kmeans.*;

public class Scml{
	/**
	 * 参数介绍
	 * @return 
	 */
	public int[] function(int[][][] A, int K, double lambda){
				
		//N是节点个数；
		int N = A[1].length;
		//M是网络层数
		int M = A.length;
		
		//设置要求的矩阵
		//定义归一化邻接矩阵 L 为 N*N*M 的零矩阵
		double[][][] L = new double[M][N][N];
		//定义拉普拉斯矩阵 L2 为 N*N*M 的零矩阵
		double[][][] L2 = new double[M][N][N];
		//定义特征值矩向量矩阵 V 为 N*K*M 的零矩阵
		double[][][] V = new double[M][N][K];
		//定义最小K个特征值对角阵 D 为 K*K*M 的零矩阵
		double[][][] D = new double[M][K][K];
		
/*
 * *************计算归一化拉普拉斯算子，得到N*N*M 的矩阵L2*****************************		
 */	
		//先生成一个N*N的单位矩阵I
		int[][] I = new int[N][N];
		for(int i = 0; i < N; i++){
			for(int j = 0; j < N; j++){
				if(i == j){
					I[i][j] = 1;
				}
			}
		}
		//调用normadj函数计算归一化邻接矩阵L
		//归一化拉普拉斯算子L2=单位矩阵I-归一化邻接矩阵L
		for(int i = 0 ; i < M ; i++){
			L[i] = normadj(A[i]);
			for(int j = 0; j < N; j++){
				for(int k = 0; k < N; k++){
					L2[i][j][k] = I[j][k] - L[i][j][k];
				}
			}
		}	
		
		//计算L2的特征向量，调用Jama包做矩阵运算	
		for(int i = 0 ; i < M ; i++){
			Matrix L2_matrix = new Matrix(L2[i]);
			//计算的L2的特征向量矩阵是V，是N*K*M的
			for(int j = 0; j < N; j++){
				for(int k = 0; k < K; k++){
					V[i][j][k] = L2_matrix.eig().getV().get(j, k);
				}				
			}
			//计算的特征值矩阵是D，是K*K*M的
			for(int j = 0; j < K; j++){
				for(int k = 0; k < K; k++){
					D[i][j][k] = L2_matrix.eig().getD().get(j, k);
				}
			}
		}

/*
 * *************compute modified Laplacian:N*N 的矩阵Lmod*****************************		
 */	
		// compute modified Laplacian
		double[][] Lmod = new double[N][N];
		double[][][] Vtans = new double[M][K][N];
		double[][][] VmultiVtans = new double[M][N][N];
		//求V的转置矩阵Vtans
		for(int i = 0; i < M; i++){ 
			for(int j = 0; j < K; j++){
				for(int k = 0; k < N; k++){
					Vtans[i][j][k] = V[i][k][j];
				}
			}
		}
		for(int i = 0; i < M; i++){
			//引用矩阵乘积函数multiplyMatrix
			VmultiVtans[i] = multiplyMatrix(V[i] , Vtans[i]);
			for(int j = 0; j < N; j++){
				for(int k = 0; k < N; k++){
					Lmod[j][k] += VmultiVtans[i][j][k];				
				}			
			}			
		}

/*
 * *************compute the representative subspace:N*N 的矩阵Lnew*****************************		
 */	
		//计算矩阵的和L2sum
		double[][] L2sum = new double [N][N];
		for(int i = 0; i < M; i++){
			
			for(int j = 0; j < N; j++){
				for(int k = 0; k < N; k++){
					L2sum[j][k] += L2[i][j][k];
				}
			}
		}
		//计算modified Laplacian矩阵Lnew，是N*N的
		double[][] Lnew = new double [N][N];
		for(int i = 0; i < N; i++){
			for(int j = 0; j < N; j++){
				Lnew[i][j] =  L2sum[i][j] - lambda*Lmod[i][j];
			}
		}
		
		//计算Lnew的特征向量矩阵是Vnew，是N*K的
		double[][] Vnew = new double [N][K];
		Matrix Lnew_matrix = new Matrix(Lnew);
		for(int j = 0; j < N; j++){
			for(int k = 0; k < K; k++){
				Vnew[j][k] = Lnew_matrix.eig().getV().get(j, k);
			}				
		}
		
		//将Vnew标准化得到N*K的矩阵Vk
		double[][] Vk = new double [N][K];
		double[][] Vnewnew = new double [1][K];
		for(int i = 0; i < N; i++){
			double norm = 0;
			Vnewnew[0] = Vnew[i];
			Matrix MM = new Matrix(Vnewnew);
			norm = MM.norm2();
			for(int j = 0; j < K; j++){
				if(norm != 0){
				Vk[i][j] = Vnew[i][j]/norm;	
				}else{
					Vk[i][j] = Vnew[i][j];
				}
			}
		}

/*
 * *************对Vk做k-means聚类得到最后结果*****************************		
 */
		kmeans_data data = new kmeans_data(Vk, N , K);
		kmeans_param param = new kmeans_param();
		param.initCenterMehtod = kmeans_param.CENTER_RANDOM;
		
		kmeans.doKmeans(K, data, param);
		//System.out.print("The labels of points is: ");
	   // for (int lable : data.labels) {
	  //      System.out.print(lable + "  ");
	  //  }
	    return data.labels;
	    
	    
	}

//定义使用的中间方法
	
/*计算归一化拉普拉斯算子的方法normadj
	返回值：double型N*N的矩阵
	输入参数：整型N*N的矩阵
*/
public static double[][] normadj(int[][] A){
	//定义参数
	int N = A[1].length;
	double sum[] = new double[N];
	
	//将A按列求和得到一个sum行向量是1*N的
	for(int i = 0; i < N; i++){
		for(int j = 0; j < N; j++){
			sum[i] += A[i][j];
		}
	}
	
	//取A的对角线得到一个向量diagw，是1*N的
	double[] diagw = new double[N];
	for(int i = 0; i < N; i++){
		diagw[i] = A[i][i];
	}
	
	//寻找A中不为零的数的位置，返回行坐标ni，列坐标nj和数值w
	//先计算不为零的数的总个数nlength2
	int nlength2 = 0;
	for(int j = 0 ; j < N ; j++){   
		for(int i = 0; i < N; i++){
			if(A[i][j] != 0){   //因为要按列查找，所以先j后i
				nlength2++;
			}
		}
	}
	//定义行坐标ni，列坐标nj和数值w向量
	int[] ni = new int [nlength2];
	int[] nj = new int [nlength2];
	double[] w  = new double [nlength2];
	int nlength = 0;
	for(int j = 0 ; j < N ; j++){   
		for(int i = 0; i < N; i++){
			if(A[j][i] != 0){   //因为要按列查找，所以先j后i
				nlength++;
				ni[nlength-1] = i+1;
				nj[nlength-1] = j+1;
				w[nlength-1]  = A[i][j];
			}
		}
	}
	
/*	//matlab中定义了一个dl向量，是1*N的向量，java没有用到
	double[] dl = new double[N];
	for(int i = 0; i < N; i++){
		dl[i] = (diagw[i]/sum[i]);
		System.out.print(dl[i]+" ");
	}
	System.out.println();
*/
	
	//计算ndl，是1*nlength的向量
	double[] ndl = new double[nlength2];
	for(int i = 0; i < nlength2; i++){
		ndl[i] = w[i]/(Math.sqrt(sum[ni[i]-1]*sum[nj[i]-1]));
	}
	
	//生成L，是N*N的向量
	double[][] L = new double[N][N];
	for(int i = 0; i < nlength ; i++){			
			L[ni[i]-1][nj[i]-1] = ndl[i];			
	}
	
	return L;
}

/*矩阵相乘的方法：multiplyMatrix
返回值：double型矩阵
输入参数：两个double型的矩阵
*/
	public static double[][] multiplyMatrix(double[][] a,double[][] b){
	
	     if(a[0].length != b.length) {
	            return null;
	     }
	     double[][] c=new double[a.length][b[0].length];
	        for(int i=0;i<a.length;i++) {
	            for(int j=0;j<b[0].length;j++) {
	              for(int k=0;k<a[0].length;k++) {            
	            c[i][j] += a[i][k] * b[k][j]; 
	           } 
	         }
	       }
	    return c;
	
	}	





}
