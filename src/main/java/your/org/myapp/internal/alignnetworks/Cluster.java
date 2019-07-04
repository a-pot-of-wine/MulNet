package your.org.myapp.internal.alignnetworks;
import org.ujmp.core.*;
import org.ujmp.core.calculation.Calculation;

import java.util.ArrayList;
import java.util.Arrays;
//所有的order是从1开始的
//邻接矩阵中节点的序列从0开始
public class Cluster {
    /*
 The implementation extends the clustering algorithm by Alex Rodriguez
 and Alessandro Laio.
 Reference:
   Rodriguez A, Laio A. Clustering by fast search and find of density
   peaks[J]. Science, 2014, 344(6191): 1492-1496.

 INPUT:
       simMatrix (N,N): the similarity matrix of the network
       nodeIDs (N,1): the identify of each node

 OUTPUT:
       cluster_result (N, 2): the cluster result
       rho (N, 1): the local density of each node
delta (N, 1): the distance of each node from nodes of higher local density

 Modified by Peizhuo Wang <wangpeizhuo_37@163.com>
 Sep. 2016
     */


    // Compute the modularity

    public static  double modularity(Matrix result,Matrix simMatrix){
        //result中的第一列中的节点序列从0开始，第二列
        //为当前节点所在的聚类编号，从0开始
        //simMatrix中行列代表节点序号，从0开始
        double Q = 0;
        int NCLUST = (int)result.selectColumns(Calculation.Ret.NEW,1).getMaxValue()+1;
        double TS = simMatrix.getValueSum()/2.0;
        for (int i=0;i<NCLUST;i++){
            //此处node_in存储第i类聚类中的节点序号，序号是从0开始的
            ArrayList<Integer> node_in = new ArrayList<>();
            Matrix temp = result.selectColumns(Calculation.Ret.NEW,1);
            for (int k=0,j = 0;j<temp.getRowCount();j++){
                if(temp.getAsInt(j,0)==i){
                    node_in.add(k++,j);
                }
            }

            int row = node_in.size();
            double[][] tempArray = new double[row][row];
            for (int j=0;j<row;j++){
                for (int k=0;k<row;k++){
                    tempArray[j][k] = simMatrix.getAsDouble(node_in.get(j),node_in.get(k));
                }
            }
            Matrix temp2 = Matrix.Factory.importFromArray(tempArray);
            double IS = temp2.getValueSum()/2.0;
            double DS = 0,sum = 0;
            for (int j=0;j<row;j++){
                Matrix temp3 = simMatrix.selectRows(Calculation.Ret.NEW,node_in.get(j));
                sum  += temp3.getValueSum();
            }
            DS = sum;
            Q = Q + IS/TS-Math.pow(DS/(2*TS),2);
        }
        return Q;
    }

    // Compute the distance between each pair of clusters
    public  static Matrix distance_clusters(Matrix result,Matrix distance){
        //dist_cl中横纵坐标代表聚类的序号，从0开始
        int NCLUST = (int)result.selectColumns(Calculation.Ret.NEW,1).getMaxValue()+1;
        Matrix dist_cl = Matrix.Factory.ones(NCLUST,NCLUST).times(100);

        for (int i=0;i<NCLUST-1;i++){
            ArrayList<Integer> cluster_i = new ArrayList<>();
            Matrix temp = result.selectColumns(Calculation.Ret.NEW,1);
            for (int k=0,j = 0;j<temp.getRowCount();j++){
                if(temp.getAsInt(j,0)==i){
                    cluster_i.add(k++,j);
                }
            }

            for (int j=i+1;j<NCLUST;j++){
                ArrayList<Integer> cluster_j = new ArrayList<>();
                Matrix temp2 = result.selectColumns(Calculation.Ret.NEW,1);
                for (int k=0,l = 0;l<temp2.getRowCount();l++){
                    if(temp.getAsInt(l,0)==j){
                        cluster_j.add(k++,l);
                    }
                }
                double[][] arrayTemp = new double[cluster_i.size()][cluster_j.size()];
                for (int k=0;k<cluster_i.size();k++){
                    for (int l=0;l<cluster_j.size();l++){
                        arrayTemp[k][l] = distance.getAsDouble(cluster_i.get(k),cluster_j.get(l));
                    }
                }
                Matrix temp4 = Matrix.Factory.importFromArray(arrayTemp);
                int lengthOfCluster_i = cluster_i.size();
                int lengthOfCluster_j = cluster_j.size();
                double sum,finalNum;
                sum = temp4.getValueSum();
                finalNum = sum/(lengthOfCluster_i*lengthOfCluster_j);
                dist_cl.setAsDouble(finalNum,i,j);
            }
        }
        return dist_cl;
    }

    public static ArrayList<Matrix> cluster(Matrix simMatrix,Matrix nodeIDs){
        long row = simMatrix.getRowCount();
        long column = simMatrix.getColumnCount();
        Matrix ones = Matrix.Factory.ones(row,column);
        Matrix distance = ones.minus(simMatrix);

            for (int i=0;i<row;i++)
            for (int j=0;j<column;j++)
                if (i==j)
                    distance.setAsDouble(0,i,j);

        long Ncount = distance.getRowCount();
        long Ecount = Ncount*(Ncount-1)/2;
        //将稀疏矩阵存储在数组中，简化运算
        double[] xx = new double[(int)(row*column)];
        int k=0;
        for (int i=0;i<row;i++){
            for (int j=0;j<column;j++){
                for (;k<row*column;){
                        xx[k] = distance.getAsDouble(j,i);
                        k++;
                        break;
                }
            }
        }

        //后面用
        Matrix cluster_result = Matrix.Factory.zeros(Ncount,2);

        double percent = 2.0;
        long position = Math.round(Ecount*percent/100);
        Arrays.sort(xx);
        double dc = xx[(int)(position+Ncount)-1];

        // Compute rho
        Matrix gaussian_distance = Matrix.Factory.zeros(row,column);
        for(int i=0;i<row;i++){
            for (int j=0;j<column;j++){
                gaussian_distance.setAsDouble(Math.exp(-Math.pow(distance.getAsDouble(i,j),2)/(dc*dc)),i,j);
            }
        }

        for (int i=0;i<row;i++)
            for (int j=0;j<column;j++)
                if (i==j)
                    gaussian_distance.setAsDouble(0,i,j);

        Matrix rho = Matrix.Factory.zeros(gaussian_distance.getRowCount(),1);
        for (int i = 0;i<gaussian_distance.getRowCount();i++){
            rho.setAsDouble(gaussian_distance.selectRows(Calculation.Ret.NEW,i).getValueSum(),i,0);
        }

        double min = rho.getMinValue();
        double max = rho.getMaxValue();
        rho = rho.minus(Matrix.Factory.ones(Ncount,1).times(min));
        rho = rho.divide(max-min);

        // Compute delta
        double maxd = distance.getMaxValue();
        Matrix temp2 = Perturbation.sortByDescend(rho);
        Matrix rho_sorted,ordrho;
        rho_sorted = temp2.selectColumns(Calculation.Ret.NEW,0);
        //ordrho中代表排序后数据在原序列中的位置，从1开始
        ordrho = temp2.selectColumns(Calculation.Ret.NEW,1);
        Matrix delta = Matrix.Factory.zeros(Ncount, 1);
        delta.setAsDouble(-1,ordrho.getAsLong(0,0)-1,0);
        long rowOfOrdrho = ordrho.getRowCount();
        Matrix nneigh = Matrix.Factory.zeros(rowOfOrdrho,1);
        for (int ii = 1;ii<Ncount;ii++){
            delta.setAsDouble(maxd+0.00001,ordrho.getAsLong(ii,0)-1,0);
            for (int jj=0;jj<ii;jj++){
                if(distance.getAsDouble(ordrho.getAsLong(ii,0)-1,ordrho.getAsLong(jj,0)-1)<delta.getAsDouble(ordrho.getAsLong(ii,0)-1,0)){
                    delta.setAsDouble(distance.getAsDouble(ordrho.getAsLong(ii,0)-1,ordrho.getAsLong(jj,0)-1),ordrho.getAsLong(ii,0)-1,0);
                    //nneigh中存储的序列从0开始
                    nneigh.setAsDouble(ordrho.getAsDouble(jj,0)-1,ordrho.getAsLong(ii,0)-1,0);
                }
            }
        }
        delta.setAsDouble(delta.getMaxValue(),ordrho.getAsLong(0,0)-1,0);
        double minOfdelta = delta.getMinValue();
        double maxOfdelta = delta.getMaxValue();
        delta = delta.minus(Matrix.Factory.ones(Ncount,1).times(minOfdelta));
        delta = delta.divide(maxOfdelta-minOfdelta);

        // Compute gamma
        Matrix gamma = rho.times(delta);
        double minOfgamma = gamma.getMinValue();
        double maxOfgamma = gamma.getMaxValue();
        gamma = gamma.minus(Matrix.Factory.ones(Ncount,1).times(minOfgamma));
        delta = gamma.divide(maxOfgamma-minOfgamma);
  
        // Select the cluster centers
        long NCLUST = 0;
        Matrix clusters = Matrix.Factory.ones(Ncount,1).times(-1);
        Matrix gamma_sorted,ordgamma;
        Matrix temp3 = Perturbation.sortByDescend(gamma);
        gamma_sorted = temp3.selectColumns(Calculation.Ret.NEW,0);
        ordgamma = temp3.selectColumns(Calculation.Ret.NEW,1);
        
        int plus=0;
        if(gamma.getRowCount()<=20) {
        	plus=(int) (gamma.getRowCount()/2);
        }else {
        	plus=20;
        }
        double[][] arrayTemp = new double[(int)(gamma.getRowCount()-plus)][1];
        for (int i=0;i<(int)(gamma.getRowCount()-plus);i++)
            arrayTemp[i][0] = gamma_sorted.getAsDouble(i+plus,0);
        Matrix gamma_calculated = Matrix.Factory.importFromArray(arrayTemp);
        double gammamin = gamma_calculated.getMeanValue()+2.33*gamma_calculated.getStdValue();
        //center用于暂存聚类对应的聚类中心节点标号,从0开始
        ArrayList<Integer> centers = new ArrayList<Integer>();
        for (int i = 0;i<Ncount;i++){
            if (gamma_sorted.getAsDouble(i,0)<gammamin)
                break;
            //排序中的原坐标是从1开始的，这里-1，使其从0开始
            int index = ordgamma.getAsInt(i,0)-1;
            //此处聚类的聚类数从0开始，以及其对应的聚类中心也是从0开始
            NCLUST = NCLUST + 1;
            clusters.setAsDouble(NCLUST-1,index,0);
            centers.add((int)(NCLUST-1),new Integer(index));
        }

        //cl_no从0开始
        Matrix cl_no = Matrix.Factory.zeros(1,NCLUST);
        for (int i=0;i<NCLUST;i++){
            cl_no.setAsInt(i,0,i);
        }

        for (int i=0;i<Ncount;i++){
            if(clusters.getAsDouble(ordrho.getAsLong(i,0)-1,0) == -1){
                clusters.setAsDouble(clusters.getAsDouble(nneigh.getAsLong(ordrho.getAsLong(i,0)-1,0),0),ordrho.getAsLong(i,0)-1,0);
            }
        }

        /*
We merge two candidate communities with the highest average similarity and iterate until the modularity is optimal.
*/
//        System.out.println(NCLUST);
        if (NCLUST>2){
            Matrix result = Matrix.Factory.zeros(Ncount,2);
            for (int i=0;i<Ncount;i++){
                result.setAsInt(i,i,0);
                result.setAsInt(clusters.getAsInt(i,0),i,1);
            }
            double Q = modularity(result,simMatrix);
//            System.out.println(Q);

            double Qold = -100;

            //made a mistake
            //note: the best way is adopt clone method, like this:Matrix clusters_new = clusters.clone(),but...the i don't know what the class matrix's stucture is,so..... ;
            double [][] clusters_new = clusters.toDoubleArray();
            ArrayList<Integer> centers_new = centers;

            while (Q>Qold){
                //合并前进行记录当前的聚类结果，若合并后模块度变小，则返回前一刻的状态
                clusters_new = clusters.toDoubleArray();
                centers_new = centers;
                result = Matrix.Factory.zeros(Ncount,2);
                for (int i=0;i<Ncount;i++){
                    result.setAsInt(i,i,0);
                    result.setAsInt(clusters.getAsInt(i,0),i,1);
                }
                Matrix dist_cl = distance_clusters( result, distance );
                double minOfDist_cl = dist_cl.getMinValue();
                int rowTemp=0,colTemp=0;

                LOOKUP:
                for (int i=0;i<NCLUST;i++){
                    for (int j=0;j<NCLUST;j++) {
                        if (dist_cl.getAsDouble(j,i) == minOfDist_cl) {
                            rowTemp = i;
                            colTemp = j;
                            break LOOKUP;
                        }
                    }
                }

                /*
Merge these two clusters£¬and the center of the cluster with the relative high density is assigned as the new cluster center. 
                 */

                if (gamma.getAsDouble(centers.get(rowTemp),0)>gamma.getAsDouble(centers.get(colTemp),0)){
                    ArrayList<Integer> rowlist = new ArrayList<>();
                    for (int i=0,j=0;i<clusters.getRowCount();i++){
                        if (clusters.getAsInt(i,0)==colTemp)
                            rowlist.add(j++,i);
                    }
                    for (int i=0;i<rowlist.size();i++)
                        clusters.setAsInt(rowTemp,rowlist.get(i),0);
                    centers.set(colTemp,-1);
                    cl_no.setAsInt(-1,0,colTemp);
                }else{
                    ArrayList<Integer> collist = new ArrayList<>();
                    for (int i=0,j=0;i<clusters.getRowCount();i++){
                        if (clusters.getAsInt(i,0)==rowTemp)
                            collist.add(j++,i);
                    }
                    for (int i=0;i<collist.size();i++)
                        clusters.setAsInt(colTemp,collist.get(i),0);
                    centers.set(rowTemp,-1);
                    cl_no.setAsInt(-1,0,rowTemp);
                }

                ArrayList<Integer> cl_no_list = new ArrayList<>();
                for (int i=0,j=0;i<cl_no.getColumnCount();i++){
                    if (cl_no.getAsInt(0,i)>-1)
                        cl_no_list.add(j++,i);
                }
                Matrix cl_no_temp = Matrix.Factory.zeros(1,cl_no_list.size());
                for (int i=0;i<cl_no_list.size();i++)
                cl_no_temp.setAsInt(cl_no.getAsInt(0,cl_no_list.get(i)),0,i);
                cl_no = cl_no_temp;

                ArrayList<Integer> centers_list = new ArrayList<>();
                for (int i=0,j=0;i<centers.size();i++){
                    if (centers.get(i)>-1)
                        centers_list.add(j++,i);
                }
                ArrayList<Integer> centers_temp = new ArrayList<>(centers_list.size());
                for (int i=0;i<centers_list.size();i++)
                    centers_temp.add(i,centers.get(centers_list.get(i)));

                centers = centers_temp;
                NCLUST = centers.size();

                for (int i=0;i<NCLUST;i++){
                    ArrayList<Integer> rowlist = new ArrayList<>();
                    for (int j=0,l=0;j<clusters.getRowCount();j++){
                        if (clusters.getAsInt(j,0)==cl_no.getAsInt(i,0))
                            rowlist.add(l++,j);
                    }
                    for (int j=0;j<rowlist.size();j++)
                        clusters.setAsInt(i,rowlist.get(j),0);
                }

                cl_no = Matrix.Factory.zeros(1,NCLUST);
                for (int i=0;i<NCLUST;i++){
                    cl_no.setAsInt(i,0,i);
                }

//               System.out.println(Q);
                Qold = Q;
                result = Matrix.Factory.zeros(Ncount,2);
                for (int i=0;i<Ncount;i++){
                    result.setAsInt(i,i,0);
                    result.setAsInt(clusters.getAsInt(i,0),i,1);
                }
                Q = modularity(result,simMatrix);
            }
            clusters = Matrix.Factory.importFromArray(clusters_new);
            centers = centers_new;
            NCLUST = centers.size();
        }

        System.out.println("NUMBER OF CLUSTERS: "+NCLUST+"\n");

        for (int i=0;i<Ncount;i++){
            cluster_result.setAsInt(nodeIDs.getAsInt(i,0),i,0);
            cluster_result.setAsInt(clusters.getAsInt(i,0),i,1);
        }

        ArrayList<Matrix> resultFinal = new ArrayList<Matrix>(3);
        resultFinal.add(0,cluster_result);
        resultFinal.add(1,rho);
        resultFinal.add(2,delta);
        return resultFinal;
    }
}
