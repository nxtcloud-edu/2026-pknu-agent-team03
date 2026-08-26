package com.timeback.ui.feature.timeline;

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
 * UI-03 타임라인 화면.
 *
 * 처리하는 ScreenState:
 * - Loading: 날짜별 데이터 로딩 중
 * - Content(TimelineViewData): 시간순 앱 사용 내역 리스트 표시
 * - Refreshing: 당겨서 새로고침 중 (이전 데이터 유지)
 * - Empty: 해당 날짜에 데이터 없음
 * - Error: 데이터 로딩 실패
 *
 * 사용자 액션:
 * - 날짜 선택 (date picker)
 * - 활동 생성/수정
 * - 맥락 확인/변경
 */
@AndroidEntryPoint
public class TimelineFragment extends Fragment {

    private TimelineViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TimelineViewModel.class);
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
                // TODO: TimelineViewData로 RecyclerView 갱신
                //   - 각 TimelineItem을 카드로 표시
                //   - 앱 이름, 활동명, 시간대, 분류 색상
                //   - contextConfirmationRequired 시 확인 버튼
            } else if (state instanceof ScreenState.Empty) {
                // TODO: 빈 상태 안내
            } else if (state instanceof ScreenState.Error) {
                // TODO: 에러 + 재시도
            }
        });

        viewModel.actionResult.observe(getViewLifecycleOwner(), result -> {
            // TODO: 활동 생성/수정 결과 처리 (토스트 또는 스낵바)
        });
    }
}
