package your.org.myapp.internal.alignnetworks;
import org.ujmp.core.*;
import org.ujmp.core.calculation.Calculation;

public class Perturbation {
    //对列向量进行递减排序，并记录原先的位置坐标，以矩阵的形式返回（返回的序号以1开始）
    public static Matrix sortByDescend(Matrix x){
        long row = x.getRowCount();
        //array1用于存储原数据，array2用于存储原数据对应的坐标值，坐标以1开始
        double[][] array0 = x.toDoubleArray();
        double[] array1 = new double[(int)row+1];
        for (int i=1;i<=row;i++)
            array1[i]=array0[i-1][0];
        int[] array2 = new int[(int)row+1];
        for (int i=1;i<=row;i++)
            array2[i] = i;

        int i,j,k,m;
        double n;
        //选择法排序
        for(i=1;i<=row-1;i++) {
            k=i;
            for(j=i+1;j<=row;j++)
                if(array1[j]>array1[k])
                    k=j;
            //交换array1[k]和array1[i]
            n=array1[k];
            array1[k]=array1[i];
            array1[i]=n;
            //交换array2[k]和array2[i],记录位置随数据交换的同时也要改变
            m=array2[k];
            array2[k]=array2[i];
            array2[i]=m;
        }
        double[][] array3 = new double[(int)row][2];
        for(i=0;i<row;i++){
            array3[i][0]=array1[i+1];
            array3[i][1]=array2[i+1];
        }
        Matrix result = Matrix.Factory.importFromArray(array3);
        return result;
    }

    public static Matrix PerturbationSim(Matrix deltaA,Matrix V,Matrix D){
        /*
         Compute the perturbated matrix based on a perturbation term and the
         eigenvectors and eigenvectors of the original matrix
        
         INPUT:
               deltaA (N,N): the pertubation between two matrices
               V (N,N): each column is a eigenvector of the original matrix
               D (N,N): the diagonal elements are eigenvalues corresponding to the
                        eigenvectors
        
         OUTPUT:
               Ap (N,N): the final perturbated matrix
        
         Author: Peizhuo Wang <wangpeizhuo_37@163.com>
         Sep. 2016
        */

        long row = deltaA.getRowCount();
        Matrix temp0 = Matrix.Factory.zeros(D.getRowCount(),1);
        for (int i=0;i<temp0.getRowCount();i++)
            temp0.setAsDouble(Math.abs(D.getAsDouble(i,i)),i,0);
        Matrix temp1 = sortByDescend(temp0);

        //Y中存储排序后的数据，I中存储Y中相应位置的数据在原序列中的位置，以1开始
        Matrix Y = temp1.selectColumns(Calculation.Ret.NEW,0);
        Matrix I = temp1.selectColumns(Calculation.Ret.NEW,1);

        Matrix cumsumY = Matrix.Factory.zeros(Y.getRowCount(),1);
        double sum = 0,sumY = Y.getValueSum();
        for (int i=0;i<Y.getRowCount();i++){
            sum+=Y.getAsDouble(i,0);
            cumsumY.setAsDouble(sum,i,0);
        }

        Matrix cs = cumsumY.divide(sumY);
        boolean[] indexY = new boolean[(int)Y.getRowCount()];
        int r = 0;
        for (int i=0;i<cs.getRowCount();i++){
            if (cs.getAsDouble(i,0)<0.9){
                r++;
                indexY[i] = true;
            }else{
                indexY[i] = false;
            }
        }
        Matrix Ymin = Matrix.Factory.zeros(r,1);
        for (int i=0,j=0;i<indexY.length;i++){
            for(;j<r;){
                if(indexY[i]){
                    Ymin.setAsDouble(Y.getAsDouble(i,0),j,0);
                    j++;
                    break;
                }else
                    break;
            }
        }

        Matrix Ddelta = Matrix.Factory.zeros(row,row);
      
        for (int i=0;i<r;i++){
            //index是从0开始的
            int index = I.getAsInt(i,0)-1;
            Matrix x = V.selectColumns(Calculation.Ret.NEW,index);
            Matrix x_T = x.transpose();
            double temp2 = x_T.mtimes(deltaA).mtimes(x).getAsDouble(0,0) / x_T.mtimes(x).getAsDouble(0,0);
            Ddelta.setAsDouble(temp2,index,index);
        }

        int[] temp3 = new int[(int)(I.getRowCount()-r)];
        for (int i=0;i<temp3.length;i++)
            temp3[i]=I.getAsInt(i+r,0)-1;
        for(int i=0;i<temp3.length;i++)
            for (int j=0;j<temp3.length;j++)
                D.setAsDouble(0,temp3[i],temp3[j]);
        Matrix Ap = V.mtimes(D.plus(Ddelta)).mtimes(V.transpose());
        for (int i=0;i<Ap.getRowCount();i++){
            for (int j=0;j<Ap.getColumnCount();j++){
                if(Ap.getAsDouble(i,j)<0)
                    Ap.setAsDouble(0,i,j);
            }
        }

        Matrix ones = Matrix.Factory.ones(row,row);
        double Ap_max = Ap.getMaxValue();
        double Ap_min = Ap.getMinValue();
        Ap = Ap.minus(ones.times(Ap_min));
        Ap = Ap.divide(Ap_max-Ap_min);

        for (int i=0;i<Ap.getRowCount();i++){
            for (int j=0;j<Ap.getColumnCount();j++){
                Ap.setAsDouble(1/(1+Math.exp(Math.log(9999)-2*Math.log(9999)*Ap.getAsDouble(i,j))),i,j);
            }
        }
        return Ap;
    }
}
