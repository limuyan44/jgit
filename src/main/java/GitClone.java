import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

/**
 * @Description
 * @Author duanmulixiang
 * @create 2018-05-09 15:51
 * @Version 1.0
 **/
public class GitClone {
    public static void main(String[] args) throws IOException, GitAPIException, InterruptedException {
//        common.git  "usr" "common" "ord" "cmt" "mkt" "cop" "ipf" "bth" "lvs"  "mnt" "getway" "mgt" vx-merchant-pc vx-mgt-pc vx-user-mobile vx-user-pc
            String REMOTE_URL_BASE = "ssh://duanmlx@66.6.52.130:29418/ifp/";
//        String[] array = {"usr","common","ord","cmt" ,"mkt", "cop", "ipf" ,"bth", "lvs" , "mnt", "getway", "mgt", "vx-merchant-pc", "vx-mgt-pc", "vx-user-mobile", "vx-user-pc"};
        String[] array = {"test"};
        ExecutorService executorService = Executors.newCachedThreadPool();
//        ExecutorService executorService = Executors.newFixedThreadPool(5);
        CountDownLatch countDownLatch = new CountDownLatch(array.length);
        File file = new File("F:\\gitCherryPick");
        for (String s : array) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    File file1 = new File(file.getPath() + File.separator + s);
                    try {
                        gitTagAdd(file1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (GitAPIException e) {
                        e.printStackTrace();
                    }

                }
            });

        }

        countDownLatch.await();
        System.out.println("结束");
        executorService.shutdownNow();


    }

    static String gitTagAdd(File file) throws IOException, GitAPIException {
        String result = "";
        Git git = null;
        try {
            git = Git.open(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        File lockFile = new File(file.getAbsolutePath() + File.separator + ".git\\index.lock");
        if (lockFile.exists()) {
            lockFile.delete();
        }
        git.reset().setMode(ResetCommand.ResetType.HARD).setRef(git.getRepository().findRef("HEAD").getName()).call();
        //说明当前分支是dev要切换到master
        if (git.getRepository().resolve("dev")!=null){
            git.checkout().setCreateBranch(false).setName("master").call();
        }
        try {
            git.pull().setCredentialsProvider(new UsernamePasswordCredentialsProvider("duanmlx", "duanmlx")).setRebase(true).call();
            PersonIdent personIdent = new PersonIdent(git.getRepository());
            git.tag().setTagger(personIdent).setMessage("mess").setName("name").call();
            git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider("duanmlx", "duanmlx")).call();
        }catch (Exception e){
            String error = "代码拉取失败：" + file.getName();
            e.printStackTrace();
            return error;
        }
        git.close();
        return result;
    }
}
