/*
 * Program:   tiny_gp.java
 *
 * Author:    Riccardo Poli (email: rpoli@essex.ac.uk)
 *
 */
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.interfaces.IExpr;

import java.util.*;
import java.io.*;

public class tiny_gp {
    double [] fitness;
    char [][] pop;
    static Random rd = new Random();
    static final int
            ADD = 110,
            SUB = 111,
            MUL = 112,
            DIV = 113,
            SIN = 114,
            COS = 115,
            FSET_START = ADD,
            FSET_END = COS;
    static double [] x = new double[FSET_START];
    static double minrandom, maxrandom;
    static char [] program;
    static int PC;
    static int varnumber, fitnesscases, randomnumber;
    static double fbestpop = 0.0, favgpop = 0.0;
    static long seed;
    static double avg_len;
    static final int
                MAX_LEN = 10000,
                POPSIZE = 100000,
                DEPTH   = 5,
                GENERATIONS = 100,
                TSIZE = 2;
    public static final double
            PMUT_PER_NODE  = 0.05,
                       CROSSOVER_PROB = 0.9;
    static double [][] targets;

    double run() { /* Interpreter */
        char primitive = program[PC++];
        if ( primitive < FSET_START )
            return(x[primitive]);

        switch ( primitive ) {
            case ADD : return( run() + run() );
            case SUB : return( run() - run() );
            case MUL : return( run() * run() );
            case DIV : {
                           double num = run(), den = run();
                           if ( Math.abs( den ) <= 0.001 )
                               return( num );
                           else
                               return( num / den );
                       }
            case SIN: { return Math.sin(Math.toRadians(run())); }
            case COS: { return Math.cos(Math.toRadians(run())); }
        }
        return( 0.0 ); // should never get here
    }

    int traverse( char [] buffer, int buffercount ) {
        if ( buffer[buffercount] < FSET_START )
            return( ++buffercount );

        return switch (buffer[buffercount]) {
            case ADD, SUB, MUL, DIV, SIN, COS -> (traverse(buffer, traverse(buffer, ++buffercount)));
            default -> (0);
        };
        // should never get here
    }

    void setup_fitness(String fname) {
        try {
            int i,j;
            String line;

            BufferedReader in =
                new BufferedReader(
                        new
                        FileReader(fname));
            line = in.readLine();
            StringTokenizer tokens = new StringTokenizer(line);
            varnumber = Integer.parseInt(tokens.nextToken().trim());
            randomnumber = Integer.parseInt(tokens.nextToken().trim());
            minrandom =	Double.parseDouble(tokens.nextToken().trim());
            maxrandom =  Double.parseDouble(tokens.nextToken().trim());
            fitnesscases = Integer.parseInt(tokens.nextToken().trim());
            targets = new double[fitnesscases][varnumber+1];
            if (varnumber + randomnumber >= FSET_START )
                System.out.println("too many variables and constants");

            for (i = 0; i < fitnesscases; i ++ ) {
                line = in.readLine();
                tokens = new StringTokenizer(line);
                for (j = 0; j <= varnumber; j++) {
                    targets[i][j] = Double.parseDouble(tokens.nextToken().trim());
                }
            }
            in.close();
        }
        catch(FileNotFoundException e) {
            System.out.println("ERROR: Please provide a data file");
            System.exit(0);
        }
        catch(Exception e ) {
            System.out.println("ERROR: Incorrect data format");
            System.exit(0);
        }
    }

    double fitness_function( char [] Prog ) {
        int i = 0, len;
        double result, fit = 0.0;

        len = traverse( Prog, 0 );
        for (i = 0; i < fitnesscases; i ++ ) {
            if (varnumber >= 0) System.arraycopy(targets[i], 0, x, 0, varnumber);
            program = Prog;
            PC = 0;
            result = run();
            fit += Math.abs( result - targets[i][varnumber]);
        }
        return(-fit );
    }

    int grow( char [] buffer, int pos, int max, int depth ) {
        char prim = (char) rd.nextInt(2);
        int one_child;

        if ( pos >= max )
            return( -1 );

        if ( pos == 0 )
            prim = 1;

        if ( prim == 0 || depth == 0 ) {
            prim = (char) rd.nextInt(varnumber + randomnumber);
            buffer[pos] = prim;
            return(pos+1);
        }
        else  {
            prim = (char) (rd.nextInt(FSET_END - FSET_START + 1) + FSET_START);
            switch (prim) {
                case ADD, SUB, MUL, DIV, SIN, COS -> {
                    buffer[pos] = prim;
                    one_child = grow(buffer, pos + 1, max, depth - 1);
                    if (one_child < 0)
                        return (-1);
                    return (grow(buffer, one_child, max, depth - 1));
                }
            }
        }
        return( 0 ); // should never get here
    }

    int print_indiv( char []buffer, int buffercounter, StringBuilder builder ) {

        int a1=0, a2;
        if ( buffer[buffercounter] < FSET_START )
        {
            if ( buffer[buffercounter] < varnumber )
                builder.append("X").append(buffer[buffercounter] + 1).append(" ");
            else
                builder.append( x[buffer[buffercounter]]);
            return( ++buffercounter );
        }
        switch (buffer[buffercounter]) {
            case ADD:
                builder.append("(");
                a1 = print_indiv(buffer, ++buffercounter, builder);
                builder.append(" + ");
                break;

            case SUB:
                builder.append("(");
                a1 = print_indiv(buffer, ++buffercounter, builder);
                builder.append(" - ");
                break;

            case MUL:
                builder.append("(");
                a1 = print_indiv(buffer, ++buffercounter, builder);
                builder.append(" * ");
                break;

            case DIV:
                builder.append("(");
                a1 = print_indiv(buffer, ++buffercounter, builder);
                builder.append(" / ");
                break;

            case SIN:
                builder.append("sin(");
                a1 = print_indiv(buffer, ++buffercounter, builder);
                builder.append(")");
                return a1;

            case COS:
                builder.append("cos(");
                a1 = print_indiv(buffer, ++buffercounter, builder);
                builder.append(")");
                return a1;
        }
        a2=print_indiv( buffer, a1, builder );
        builder.append( ")");
        return( a2);
    }


    static char [] buffer = new char[MAX_LEN];
    char [] create_random_indiv() {
        char [] ind;
        int len;

        len = grow( buffer, 0, MAX_LEN, tiny_gp.DEPTH);

        while (len < 0 )
            len = grow( buffer, 0, MAX_LEN, tiny_gp.DEPTH);

        ind = new char[len];

        System.arraycopy(buffer, 0, ind, 0, len );
        return( ind );
    }

    char [][] create_random_pop(double[] fitness) {
        char [][]pop = new char[tiny_gp.POPSIZE][];
        int i;

        for (i = 0; i < tiny_gp.POPSIZE; i ++ ) {
            pop[i] = create_random_indiv();
            fitness[i] = fitness_function( pop[i] );
        }
        return( pop );
    }


    void stats( double [] fitness, char [][] pop, int gen ) {
        int i, best = rd.nextInt(POPSIZE);
        int node_count = 0;
        fbestpop = fitness[best];
        favgpop = 0.0;

        for ( i = 0; i < POPSIZE; i ++ ) {
            node_count +=  traverse( pop[i], 0 );
            favgpop += fitness[i];
            if ( fitness[i] > fbestpop ) {
                best = i;
                fbestpop = fitness[i];
            }
        }
        avg_len = (double) node_count / POPSIZE;
        favgpop /= POPSIZE;

       String s ="Generation="+gen+" Avg Fitness="+(-favgpop)+
                " Best Fitness="+(-fbestpop)+" Avg Size="+avg_len+
                "\nBest Individual: ";
        System.out.print(s);
        //save_to_file("results/1.txt", s);
        StringBuilder builder = new StringBuilder();

        //Simplifier simplifier = new Simplifier();
        print_indiv( pop[best], 0, builder );
        System.out.println(builder);

        String tmp = optimize(builder.toString());
        //save_to_file("results/1.txt", builder.toString());
        System.out.println(tmp);
        System.out.print( "\n");
        System.out.flush();
    }
    //pisane na szybko trzeba poprawić
    void save_to_file(String path, String value){
        try {
            FileWriter fw = new FileWriter(path, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(value);
            bw.newLine();
            bw.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    int tournament(double[] fitness) {
        int best = rd.nextInt(POPSIZE), i, competitor;
        double  fbest = -1.0e34;

        for (i = 0; i < tiny_gp.TSIZE; i ++ ) {
            competitor = rd.nextInt(POPSIZE);
            if ( fitness[competitor] > fbest ) {
                fbest = fitness[competitor];
                best = competitor;
            }
        }
        return( best );
    }

    int negative_tournament(double[] fitness) {
        int worst = rd.nextInt(POPSIZE), i, competitor;
        double fworst = 1e34;

        for (i = 0; i < tiny_gp.TSIZE; i ++ ) {
            competitor = rd.nextInt(POPSIZE);
            if ( fitness[competitor] < fworst ) {
                fworst = fitness[competitor];
                worst = competitor;
            }
        }
        return( worst );
    }

    char [] crossover( char []parent1, char [] parent2 ) {
        int xo1start, xo1end, xo2start, xo2end;
        char [] offspring;
        int len1 = traverse( parent1, 0 );
        int len2 = traverse( parent2, 0 );
        int lenoff;

        xo1start =  rd.nextInt(len1);
        xo1end = traverse( parent1, xo1start );

        xo2start =  rd.nextInt(len2);
        xo2end = traverse( parent2, xo2start );

        lenoff = xo1start + (xo2end - xo2start) + (len1-xo1end);

        offspring = new char[lenoff];

        System.arraycopy( parent1, 0, offspring, 0, xo1start );
        System.arraycopy( parent2, xo2start, offspring, xo1start,
                (xo2end - xo2start) );
        System.arraycopy( parent1, xo1end, offspring,
                xo1start + (xo2end - xo2start),
                (len1-xo1end) );

        return( offspring );
    }

    char [] mutation(char[] parent) {
        int len = traverse( parent, 0 ), i;
        int mutsite;
        char [] parentcopy = new char [len];

        System.arraycopy( parent, 0, parentcopy, 0, len );
        for (i = 0; i < len; i ++ ) {
            if ( rd.nextDouble() < tiny_gp.PMUT_PER_NODE) {
                mutsite =  i;
                if ( parentcopy[mutsite] < FSET_START )
                    parentcopy[mutsite] = (char) rd.nextInt(varnumber+randomnumber);
                else
                    switch (parentcopy[mutsite]) {
                        case ADD, SUB, MUL, DIV, SIN, COS -> parentcopy[mutsite] =
                                (char) (rd.nextInt(FSET_END - FSET_START + 1)
                                        + FSET_START);
                    }
            }
        }
        return( parentcopy );
    }

    void print_parms() {
        System.out.print("-- TINY GP (Java version) --\n");
        System.out.print("SEED="+seed+"\nMAX_LEN="+MAX_LEN+
                "\nPOPSIZE="+POPSIZE+"\nDEPTH="+DEPTH+
                "\nCROSSOVER_PROB="+CROSSOVER_PROB+
                "\nPMUT_PER_NODE="+PMUT_PER_NODE+
                "\nMIN_RANDOM="+minrandom+
                "\nMAX_RANDOM="+maxrandom+
                "\nGENERATIONS="+GENERATIONS+
                "\nTSIZE="+TSIZE+
                "\n----------------------------------\n");
    }

    public tiny_gp( String fname, long s ) {
        fitness =  new double[POPSIZE];
        seed = s;
        if ( seed >= 0 )
            rd.setSeed(seed);
        setup_fitness(fname);
        for ( int i = 0; i < FSET_START; i ++ )
            x[i]= (maxrandom-minrandom)*rd.nextDouble()+minrandom;
        pop = create_random_pop(fitness );
    }

    public String optimize(String function) {

        var functionWithArg = function.replace("X1", "x").replace("X2", "y");
        ExprEvaluator evaluator = new ExprEvaluator();
        IExpr result = evaluator.eval(functionWithArg);

        return result.toString();
    }

    void evolve() {
        int gen = 0, indivs, offspring, parent1, parent2, parent;
        double newfit;
        char []newind;
        print_parms();
        stats( fitness, pop, 0 );
        for ( gen = 1; gen < GENERATIONS; gen ++ ) {
            if (  fbestpop > -1e-5 ) {
                System.out.print("PROBLEM SOLVED\n");
                System.exit( 0 );
            }
            for ( indivs = 0; indivs < POPSIZE; indivs ++ ) {
                if ( rd.nextDouble() < CROSSOVER_PROB  ) {
                    parent1 = tournament( fitness);
                    parent2 = tournament( fitness);
                    newind = crossover( pop[parent1],pop[parent2] );
                }
                else {
                    parent = tournament( fitness);
                    newind = mutation( pop[parent]);
                }
                newfit = fitness_function( newind );
                offspring = negative_tournament( fitness);
                pop[offspring] = newind;
                fitness[offspring] = newfit;
            }
            stats( fitness, pop, gen );
        }
        System.out.print("PROBLEM *NOT* SOLVED\n");
        System.exit( 1 );
    }

    public static void main(String[] args) {
        String fname = "problem.dat";
        long s = 406277;

        if ( args.length == 2 ) {
            s = Integer.parseInt(args[0]);
            fname = args[1];
        }
        if ( args.length == 1 ) {
            fname = args[0];
        }

        tiny_gp gp = new tiny_gp(fname, s);
        gp.evolve();

        /* EXCEL DEMO
        ExcelExporter excelExporter = new ExcelExporter("../DatFiles/1_1.dat");
        excelExporter.addSheet();
        excelExporter.putDATColumns();
        excelExporter.exportToFile( "temp.xlsx");
         */

    }
};
