package your.org.myapp.internal.alignnetworks;
/*
 * �Զ������Scml
 * ����ӿڹ淶��ʵ�ֽӿڣ���д���󷽷�
 */
/**
 * class����
 * 
 */
/*SC-ML�㷨��ʵ�֣�
	sc-ml(A, K, lambda)
	���룺
		A:N*N*M�Ķ���ڽӾ���N��N��M��
		K�����������
		lambda�����򻯲�����
	�����
		ÿ����ľ����ǩ
*/



import Jama.Matrix;
import kmeans.*;

public class Scml{
	/**
	 * ��������
	 * @return 
	 */
	public int[] function(int[][][] A, int K, double lambda){
				
		//N�ǽڵ������
		int N = A[1].length;
		//M���������
		int M = A.length;
		
		//����Ҫ��ľ���
		//�����һ���ڽӾ��� L Ϊ N*N*M �������
		double[][][] L = new double[M][N][N];
		//����������˹���� L2 Ϊ N*N*M �������
		double[][][] L2 = new double[M][N][N];
		//��������ֵ���������� V Ϊ N*K*M �������
		double[][][] V = new double[M][N][K];
		//������СK������ֵ�Խ��� D Ϊ K*K*M �������
		double[][][] D = new double[M][K][K];
		
/*
 * *************�����һ��������˹���ӣ��õ�N*N*M �ľ���L2*****************************		
 */	
		//������һ��N*N�ĵ�λ����I
		int[][] I = new int[N][N];
		for(int i = 0; i < N; i++){
			for(int j = 0; j < N; j++){
				if(i == j){
					I[i][j] = 1;
				}
			}
		}
		//����normadj���������һ���ڽӾ���L
		//��һ��������˹����L2=��λ����I-��һ���ڽӾ���L
		for(int i = 0 ; i < M ; i++){
			L[i] = normadj(A[i]);
			for(int j = 0; j < N; j++){
				for(int k = 0; k < N; k++){
					L2[i][j][k] = I[j][k] - L[i][j][k];
				}
			}
		}	
		
		//����L2����������������Jama������������	
		for(int i = 0 ; i < M ; i++){
			Matrix L2_matrix = new Matrix(L2[i]);
			//�����L2����������������V����N*K*M��
			for(int j = 0; j < N; j++){
				for(int k = 0; k < K; k++){
					V[i][j][k] = L2_matrix.eig().getV().get(j, k);
				}				
			}
			//���������ֵ������D����K*K*M��
			for(int j = 0; j < K; j++){
				for(int k = 0; k < K; k++){
					D[i][j][k] = L2_matrix.eig().getD().get(j, k);
				}
			}
		}

/*
 * *************compute modified Laplacian:N*N �ľ���Lmod*****************************		
 */	
		// compute modified Laplacian
		double[][] Lmod = new double[N][N];
		double[][][] Vtans = new double[M][K][N];
		double[][][] VmultiVtans = new double[M][N][N];
		//��V��ת�þ���Vtans
		for(int i = 0; i < M; i++){ 
			for(int j = 0; j < K; j++){
				for(int k = 0; k < N; k++){
					Vtans[i][j][k] = V[i][k][j];
				}
			}
		}
		for(int i = 0; i < M; i++){
			//���þ���˻�����multiplyMatrix
			VmultiVtans[i] = multiplyMatrix(V[i] , Vtans[i]);
			for(int j = 0; j < N; j++){
				for(int k = 0; k < N; k++){
					Lmod[j][k] += VmultiVtans[i][j][k];				
				}			
			}			
		}

/*
 * *************compute the representative subspace:N*N �ľ���Lnew*****************************		
 */	
		//�������ĺ�L2sum
		double[][] L2sum = new double [N][N];
		for(int i = 0; i < M; i++){
			
			for(int j = 0; j < N; j++){
				for(int k = 0; k < N; k++){
					L2sum[j][k] += L2[i][j][k];
				}
			}
		}
		//����modified Laplacian����Lnew����N*N��
		double[][] Lnew = new double [N][N];
		for(int i = 0; i < N; i++){
			for(int j = 0; j < N; j++){
				Lnew[i][j] =  L2sum[i][j] - lambda*Lmod[i][j];
			}
		}
		
		//����Lnew����������������Vnew����N*K��
		double[][] Vnew = new double [N][K];
		Matrix Lnew_matrix = new Matrix(Lnew);
		for(int j = 0; j < N; j++){
			for(int k = 0; k < K; k++){
				Vnew[j][k] = Lnew_matrix.eig().getV().get(j, k);
			}				
		}
		
		//��Vnew��׼���õ�N*K�ľ���Vk
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
 * *************��Vk��k-means����õ������*****************************		
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

//����ʹ�õ��м䷽��
	
/*�����һ��������˹���ӵķ���normadj
	����ֵ��double��N*N�ľ���
	�������������N*N�ľ���
*/
public static double[][] normadj(int[][] A){
	//�������
	int N = A[1].length;
	double sum[] = new double[N];
	
	//��A������͵õ�һ��sum��������1*N��
	for(int i = 0; i < N; i++){
		for(int j = 0; j < N; j++){
			sum[i] += A[i][j];
		}
	}
	
	//ȡA�ĶԽ��ߵõ�һ������diagw����1*N��
	double[] diagw = new double[N];
	for(int i = 0; i < N; i++){
		diagw[i] = A[i][i];
	}
	
	//Ѱ��A�в�Ϊ�������λ�ã�����������ni��������nj����ֵw
	//�ȼ��㲻Ϊ��������ܸ���nlength2
	int nlength2 = 0;
	for(int j = 0 ; j < N ; j++){   
		for(int i = 0; i < N; i++){
			if(A[i][j] != 0){   //��ΪҪ���в��ң�������j��i
				nlength2++;
			}
		}
	}
	//����������ni��������nj����ֵw����
	int[] ni = new int [nlength2];
	int[] nj = new int [nlength2];
	double[] w  = new double [nlength2];
	int nlength = 0;
	for(int j = 0 ; j < N ; j++){   
		for(int i = 0; i < N; i++){
			if(A[j][i] != 0){   //��ΪҪ���в��ң�������j��i
				nlength++;
				ni[nlength-1] = i+1;
				nj[nlength-1] = j+1;
				w[nlength-1]  = A[i][j];
			}
		}
	}
	
/*	//matlab�ж�����һ��dl��������1*N��������javaû���õ�
	double[] dl = new double[N];
	for(int i = 0; i < N; i++){
		dl[i] = (diagw[i]/sum[i]);
		System.out.print(dl[i]+" ");
	}
	System.out.println();
*/
	
	//����ndl����1*nlength������
	double[] ndl = new double[nlength2];
	for(int i = 0; i < nlength2; i++){
		ndl[i] = w[i]/(Math.sqrt(sum[ni[i]-1]*sum[nj[i]-1]));
	}
	
	//����L����N*N������
	double[][] L = new double[N][N];
	for(int i = 0; i < nlength ; i++){			
			L[ni[i]-1][nj[i]-1] = ndl[i];			
	}
	
	return L;
}

/*������˵ķ�����multiplyMatrix
����ֵ��double�;���
�������������double�͵ľ���
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
