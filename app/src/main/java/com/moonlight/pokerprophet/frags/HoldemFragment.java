package com.moonlight.pokerprophet.frags;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.moonlight.pokerprophet.Card;
import com.moonlight.pokerprophet.CustomAdapter;
import com.moonlight.pokerprophet.DataUtil;
import com.moonlight.pokerprophet.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.moonlight.pokerprophet.DataUtil.cards_curr;
import static com.moonlight.pokerprophet.DataUtil.checkHand;
import static com.moonlight.pokerprophet.DataUtil.getCurrentArray;
import static com.moonlight.pokerprophet.DataUtil.tag;


public class HoldemFragment extends Fragment {

    private static final int MAX_STAGE = 4;
    private AlertDialog.Builder builder;
    private FloatingActionButton fab;
    private TextView adCounter, adviceTxt;
    private Handler delayRun = new Handler();
    private LinearLayout linearLayout3;
    private SwipeRefreshLayout swipe;
    private TextView stage;
    private ImageButton back, info, share;

    private MaterialCardView card1, card2, card3;
    private ArrayList<MaterialCardView> cards = new ArrayList<>();
    View root;


    public HoldemFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (root == null) {
            root = inflater.inflate(R.layout.fragment_holdem, container, false);
            final FloatingActionButton share_btn = root.findViewById(R.id.share_btn);
            final FloatingActionButton info_btn = root.findViewById(R.id.info_btn);

            info_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    AlertDialog alertDialog = builder.setView(R.layout.dialog_about)
                            .show();
                    alertDialog.getWindow()
                            .setBackgroundDrawable(null);
                }
            });


            share_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "ДелисЪ");
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
                }
            });


            cards.add(root.findViewById(R.id.hand1));
            cards.add(root.findViewById(R.id.hand2));
            cards.add(root.findViewById(R.id.table1));
            cards.add(root.findViewById(R.id.table2));
            cards.add(root.findViewById(R.id.table3));
            cards.add(root.findViewById(R.id.table4));
            cards.add(root.findViewById(R.id.table5));
            card1 = root.findViewById(R.id.card1);
            card2 = root.findViewById(R.id.card2);
            card3 = root.findViewById(R.id.card3);

            back = root.findViewById(R.id.back_btn); //todo
            //share = root.findViewById(R.id.share_btn);
            //info = root.findViewById(R.id.info_btn);

            fab = root.findViewById(R.id.fab);
            adviceTxt = root.findViewById(R.id.textView);
            linearLayout3 = root.findViewById(R.id.linearLayout3);
            stage = root.findViewById(R.id.stage);
            swipe = root.findViewById(R.id.swipe);
            bottomProgressDots(1);
            linearLayout3.setAlpha(0);
            cards.subList(5, 7).forEach((c) -> c.setVisibility(View.GONE));
            card3.setVisibility(View.VISIBLE);

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Navigation.findNavController(root).navigate(R.id.action_holdemFragment_to_rulesFragment);
                }
            });

            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    DataUtil.reset();
                    Navigation.findNavController(root).popBackStack();
                }
            });


            swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (card1.getVisibility() == View.VISIBLE) {
                        TransitionManager.beginDelayedTransition(container);
                        card1.setVisibility(View.GONE);
                    }
                    if (card2.getVisibility() == View.VISIBLE) {
                        TransitionManager.beginDelayedTransition(container);
                        card2.setVisibility(View.GONE);
                    }
                    delayRun.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            cards.forEach(c -> {
                                Glide.with(root)
                                        .load(R.drawable.question)
                                        .into((ImageView) c.getChildAt(0));
                                //((ImageView) c.getChildAt(0)).setImageResource(R.drawable.question);
                                c.setClickable(true);
                                c.setTag(null);
                            });
                            cards.subList(5, 7).forEach(c -> c.setVisibility(View.GONE));
                        }
                    }, 300);

                    bottomProgressDots(1);
                    DataUtil.reset();
                    swipe.setRefreshing(false);

                }
            });
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DataUtil.ranks_c = new ArrayList<>(DataUtil.ranks);
                    DataUtil.ranks_h = new ArrayList<>(DataUtil.ranks);
                    DataUtil.ranks_s = new ArrayList<>(DataUtil.ranks);
                    DataUtil.ranks_d = new ArrayList<>(DataUtil.ranks);
                    DataUtil.cards_curr.clear();
                    cards.forEach(c -> {
                        String tag = (String) c.getTag();
                        if (tag != null)
                            switch ("" + tag.charAt(tag.length() - 1)) {
                                case "c":
                                    DataUtil.ranks_c.remove(tag.substring(0, tag.length() - 1));
                                    DataUtil.cards_curr.add(new Card("c", tag.substring(0, tag.length() - 1)));
                                    break;
                                case "h":
                                    DataUtil.ranks_h.remove(tag.substring(0, tag.length() - 1));
                                    DataUtil.cards_curr.add(new Card("h", tag.substring(0, tag.length() - 1)));
                                    break;
                                case "s":
                                    DataUtil.ranks_s.remove(tag.substring(0, tag.length() - 1));
                                    DataUtil.cards_curr.add(new Card("s", tag.substring(0, tag.length() - 1)));
                                    break;
                                case "d":
                                    DataUtil.ranks_d.remove(tag.substring(0, tag.length() - 1));
                                    DataUtil.cards_curr.add(new Card("d", tag.substring(0, tag.length() - 1)));
                                    break;
                            }
                    });

                    builder = new AlertDialog.Builder(root.getContext());
                    AlertDialog alertDialog = builder.setView(R.layout.alert_picker)
                            .show();
                    alertDialog.getWindow()
                            .setBackgroundDrawable(null);

                    //alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {

                            if ((cards.get(0).getTag() != null) && (cards.get(1).getTag() != null) && (card2.getVisibility() != View.VISIBLE)) {
                                swipe.setEnabled(false);
                                TransitionManager.beginDelayedTransition(container);
                                card2.setVisibility(View.VISIBLE);
                                bottomProgressDots(2);
                                delayRun.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {

                                        adviceTxt.setTextColor(Color.DKGRAY);
                                        // adviceTxt.setText(DataUtil.prophet());

                                    }
                                }, 400);
                                delayRun.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        TransitionManager.beginDelayedTransition(container, new AutoTransition().setDuration(300));
                                        card1.setVisibility(View.VISIBLE);
                                        swipe.setEnabled(true);
                                    }
                                }, 800);
                            }
                            if (card2.getVisibility() == View.VISIBLE) {
                                if ((cards.get(2).getTag() != null) && (cards.get(3).getTag() != null) && (cards.get(4).getTag() != null)) {
                                    TransitionManager.beginDelayedTransition(container, new AutoTransition().setDuration(300));
                                    cards.get(5).setVisibility(View.VISIBLE);
                                    bottomProgressDots(3);
                                }
                                if (cards.get(5).getTag() != null) {
                                    TransitionManager.beginDelayedTransition(container, new AutoTransition().setDuration(300));
                                    cards.get(6).setVisibility(View.VISIBLE);
                                    bottomProgressDots(4);
                                }
                                if (cards.get(6).getTag() != null) {

                                }
                            }
                            Log.wtf(tag, "Cards curr:");
                            for (Card card : DataUtil.cards_curr)
                                Log.d(tag, card.toString());
                            //Log.wtf(tag, "DataUtil.prophet: " + DataUtil.prophet());
                            //Log.wtf(tag, "Res array:");
                            //Log.wtf(tag, "" + Arrays.asList(getResources().getStringArray(R.array.result)));

                            //Integer propheti = DataUtil.prophet();
                            //Log.wtf(tag, "Integer propheti = DataUtil.prophet(); " + propheti);
//                            if (propheti != null) {
//                                String str = Arrays.asList(getResources().getStringArray(R.array.result))
//                                        .get(propheti - 1);
                            // Log.wtf(tag, "Prophet = " + str);

                            switch (cards_curr.size()) {
                                case 2:
                                    setAnimText(adviceTxt, Arrays.asList(getResources().getStringArray(R.array.result)).get(checkHand() - 1), 150);
                                    break;
                                case 5:
                                    prophet();
                                    break;
                                case 6:
                                    prophet();
                                    break;
                                case 7:
                                    prophet();
                                    break;
                            }
                        }
                    });
                    initRecycler(alertDialog, (MaterialCardView) view);
                }
            };

            cards.forEach(c -> c.setOnClickListener(onClickListener));
        }
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        System.out.println("START ================");
        linearLayout3.animate().alpha(1).setStartDelay(200).setDuration(1000).start();
    }

    private void prophet() {
        Observable.fromArray(getCurrentArray())
                .map((a) -> {
                    System.out.println(a);
                    return DataUtil.check((ArrayList) a);
                })
                .sorted()
                .firstElement()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(e -> setAnimText(adviceTxt,
                        Arrays.asList(getResources().getStringArray(R.array.result)).get((int) e - 1),
                        150));
    }

    private void initRecycler(AlertDialog alertDialog, MaterialCardView view) {
        RecyclerView recyclerSpades = alertDialog.findViewById(R.id.spades);
        RecyclerView recyclerDiamonds = alertDialog.findViewById(R.id.diamonds);
        RecyclerView recyclerHearts = alertDialog.findViewById(R.id.hearts);
        RecyclerView recyclerClubs = alertDialog.findViewById(R.id.clubs);

        recyclerSpades.setLayoutManager(getLayoutManager(alertDialog));
        recyclerDiamonds.setLayoutManager(getLayoutManager(alertDialog));
        recyclerHearts.setLayoutManager(getLayoutManager(alertDialog));
        recyclerClubs.setLayoutManager(getLayoutManager(alertDialog));

        List<Card> cardsC = DataUtil.getCards("c");
        List<Card> cardsH = DataUtil.getCards("h");
        List<Card> cardsS = DataUtil.getCards("s");
        List<Card> cardsD = DataUtil.getCards("d");

        recyclerClubs.setAdapter(new CustomAdapter(cardsC, alertDialog, view));
        recyclerDiamonds.setAdapter(new CustomAdapter(cardsD, alertDialog, view));
        recyclerHearts.setAdapter(new CustomAdapter(cardsH, alertDialog, view));
        recyclerSpades.setAdapter(new CustomAdapter(cardsS, alertDialog, view));

        new LinearSnapHelper().attachToRecyclerView(recyclerClubs);
        new LinearSnapHelper().attachToRecyclerView(recyclerDiamonds);
        new LinearSnapHelper().attachToRecyclerView(recyclerHearts);
        new LinearSnapHelper().attachToRecyclerView(recyclerSpades);
    }

    private RecyclerView.LayoutManager getLayoutManager(AlertDialog alertDialog) {
        RecyclerView.LayoutManager rvlm = new LinearLayoutManager(alertDialog.getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvlm.scrollToPosition(Integer.MAX_VALUE / 2);
        return rvlm;
    }


    private void bottomProgressDots(int current_index) {
        current_index--;
        LinearLayout dotsLayout = root.findViewById(R.id.layoutDots);
        ImageView[] dots = new ImageView[MAX_STAGE];
        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new ImageView(getContext());
            int width_height = 30;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(width_height, width_height));
            params.setMargins(10, 10, 10, 10);
            dots[i].setLayoutParams(params);
            dots[i].setImageResource(R.drawable.shape_circle);
            dots[i].setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0) {
            dots[current_index].setImageResource(R.drawable.shape_circle);
            dots[current_index].setColorFilter(R.color.colorPrimary, PorterDuff.Mode.DST);
        }

        setAnimText(stage, getResources().getStringArray(R.array.stages)[current_index], 100);


    }


    private void setAnimText(TextView text, String str, int duration) {
        if (!text.getText().equals(str))
            text.animate().alpha(0).setDuration(duration).withEndAction(new Runnable() {
                @Override
                public void run() {
                    text.setText(str);
                    text.animate().alpha(1).setDuration(duration).start();
                }
            }).start();
    }

}
