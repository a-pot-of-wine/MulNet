package your.org.myapp.internal.alignnetworks;
import java.util.ArrayList;

import org.ujmp.core.*;
import org.ujmp.core.calculation.Calculation;

public class ESPRA {
    /*
     Evolutionary clustering based on Structural Perturbation and Resource
     Allocation similarity
    
     INPUT:
           simMatrices (N,N,K): a series of symmetric similarity matrix of
                                networks
           alpha: parameter for balancing the current cluster (=1) and historical
                  influence (=0)
           beta: parameter for trading off the emphasis between the structural
                 perturbation and topological similarity
        
     OUTPUT:
          result: the clustering results at every time step
     
     alpha=0.8, beta=0.5
     Author: Peizhuo Wang <wangpeizhuo_37@163.com>
     Sep. 2016
    */
    public static ArrayList<Matrix> ESPRA_Similarity(ArrayList<Matrix> adjacencyMatrices,double alpha,double beta){
        int T = adjacencyMatrices.size();
        ArrayList<Matrix> simMatrix_perb = new ArrayList<>(T);
        Matrix A1 = adjacencyMatrices.get(0);
        Matrix A2 = adjacencyMatrices.get(1);

        Matrix[] eigenValueDecompostion = A1.eig();
        Matrix V = eigenValueDecompostion[0],D = eigenValueDecompostion[1];
        simMatrix_perb.add(0,Perturbation.PerturbationSim(A2.minus(A1),V,D).times(beta).plus(RA.RA_index(A1).times(1-beta)));

        for (int i=1;i<T;i++) {

            A1 = adjacencyMatrices.get(i - 1);
            A2 = adjacencyMatrices.get(i);
            eigenValueDecompostion = A2.eig();
            V = eigenValueDecompostion[0];
            D = eigenValueDecompostion[1];
            Matrix B = Perturbation.PerturbationSim(A1.minus(A2), V, D).times(beta).plus(RA.RA_index(A2).times(1 - beta));
            simMatrix_perb.add(i, B.times(alpha).plus(simMatrix_perb.get(i - 1).times(1 - alpha)));
        }

        return simMatrix_perb;

    }

    public static ArrayList<Matrix> ESPRA_Algorithm(ArrayList<Matrix> adjacencyMatrices,double alpha,double beta){
        int T = adjacencyMatrices.size();
        ArrayList<Matrix> result = new ArrayList<>(T);
        ArrayList<Matrix> simMatrix_perb = new ArrayList<>(T);

        // clustering for the first network
        System.out.println("timestep 1:");
        Matrix A1 = adjacencyMatrices.get(0);
        Matrix A2 = adjacencyMatrices.get(1);

        Matrix[] eigenValueDecompostion = A1.eig();
        Matrix V = eigenValueDecompostion[0],D = eigenValueDecompostion[1];
        simMatrix_perb.add(0,Perturbation.PerturbationSim(A2.minus(A1),V,D).times(beta).plus(RA.RA_index(A1).times(1-beta)));

        //nodes中的节点序号从0开始
        ArrayList<Integer> nodes = new ArrayList<>();
        for (int i=0,j=0;i<adjacencyMatrices.get(0).getColumnCount();i++){
            if(adjacencyMatrices.get(0).selectColumns(Calculation.Ret.NEW,i).getValueSum()!=0){
                nodes.add(j++,i);
            }
        }

        Matrix temp = simMatrix_perb.get(0);
        Matrix temp1 = Matrix.Factory.zeros(nodes.size(),nodes.size());
        for (int i=0;i<nodes.size();i++){
            for (int j=0;j<nodes.size();j++){
                temp1.setAsDouble(temp.getAsDouble(nodes.get(i),nodes.get(j)),i,j);
            }
        }

        int nodestRow = nodes.size();
        Matrix nodeIDs = Matrix.Factory.zeros(nodestRow,1);
        for (int i=0;i<nodestRow;i++){
            nodeIDs.setAsInt(nodes.get(i),i,0);
        }
        //nodeIDs中的节点的序号从0开始

        result.add(0,Cluster.cluster(temp1,nodeIDs).get(0));
        for (int i=1;i<T;i++){
            System.out.println("Timestep "+(i+1)+":\n");
            A1 = adjacencyMatrices.get(i-1);
            A2 = adjacencyMatrices.get(i);
            eigenValueDecompostion = A2.eig();
            V = eigenValueDecompostion[0];
            D = eigenValueDecompostion[1];
            Matrix B = Perturbation.PerturbationSim(A1.minus(A2),V,D).times(beta).plus(RA.RA_index(A2).times(1-beta));
            simMatrix_perb.add(i,B.times(alpha).plus(simMatrix_perb.get(i-1).times(1-alpha)));

            //nodes中的节点序号从0开始
            ArrayList<Integer> ano_nodes = new ArrayList<>();
            for (int j=0,k=0;j<adjacencyMatrices.get(i).getColumnCount();j++){
                if(adjacencyMatrices.get(i).selectColumns(Calculation.Ret.NEW,j).getValueSum()!=0){
                    ano_nodes.add(k++,j);
                }
            }
            temp = simMatrix_perb.get(i);
            temp1 = Matrix.Factory.zeros(ano_nodes.size(),ano_nodes.size());
            for (int j=0;j<ano_nodes.size();j++){
                for (int k=0;k<ano_nodes.size();k++){
                    temp1.setAsDouble(temp.getAsDouble(ano_nodes.get(j),ano_nodes.get(k)),j,k);
                }
            }
            nodestRow = ano_nodes.size();
            nodeIDs = Matrix.Factory.zeros(nodestRow,1);
            for (int j=0;j<nodestRow;j++){
                nodeIDs.setAsInt(ano_nodes.get(j),j,0);
            }

            result.add(i,Cluster.cluster(temp1,nodeIDs).get(0));

        }

        return result;
    }
}