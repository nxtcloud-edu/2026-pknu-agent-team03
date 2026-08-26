package com.timeback.ui.feature.goals;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.timeback.R;

import com.timeback.ui.domain.model.ScreenState;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * UI-06 목표 화면.
 *
 * 처리하는 ScreenState:
 * - Loading: 목표 목록 로딩 중
 * - Content(GoalsViewData): 목표 + 진행률 리스트 표시
 * - Empty: 목표 없음 → 첫 목표 생성 유도
 * - Error: 로딩 실패
 *
 * 사용자 액션:
 * - 새 목표 생성 (이름 + 목표 시간)
 */
@AndroidEntryPoint
public class GoalsFragment extends Fragment {

    private GoalsViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(GoalsViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_placeholder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        observeState();
    }

    private void observeState() {
        viewModel.state.observe(getViewLifecycleOwner(), state -> {
            if (state instanceof ScreenState.Loading) {
                // TODO: 로딩 표시
            } else if (state instanceof ScreenState.Content) {
                // TODO: GoalsViewData로 목표 리스트 렌더링
                //   - 목표 이름 + 목표 시간
                //   - 누적 시간 + 진행률 바
            } else if (state instanceof ScreenState.Empty) {
                // TODO: 빈 상태 + "첫 목표 만들기" 버튼
            } else if (state instanceof ScreenState.Error) {
                // TODO: 에러 + 재시도
            }
        });

        viewModel.actionResult.observe(getViewLifecycleOwner(), result -> {
            // TODO: 목표 생성 결과 피드백
        });
    }
}
